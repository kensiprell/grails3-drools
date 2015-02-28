package grails.plugin.drools

import grails.util.Environment
import groovy.xml.MarkupBuilder
import org.kie.spring.KModuleBeanFactoryPostProcessor
import static groovy.io.FileType.FILES

class ProcessDroolsResources {
	def configurationType //= grailsSettings.config.grails.plugin.drools.configurationType ?: "droolsConfigGroovy"
	def drlFileLocation //= grailsSettings.config.grails.plugin.drools.drlFileLocation ?: "src/rules"
	def sourceDir //= new File(basedir, drlFileLocation)

	def eventCompileEnd() {
		if (configurationType == "droolsConfigGroovy") {
			writeDroolsContentXml(basedir, isPluginProject)
		}
		copyFiles(buildSettings.classesDir)
	}

	def eventTestCompileEnd() {
		def integrationPath = "${grailsSettings.testClassesDir}/integration"
		def integrationDir = new File(integrationPath)
		if (integrationDir.exists()) {
			copyFiles(integrationPath)
		}
	}

	def eventCreateWarEnd() {
		copyFiles("$stagingDir/WEB-INF/classes")
	}

	static process(config) {
		String configurationType = config.grails.plugin.drools.configurationType
		String drlFileLocation = config.grails.plugin.drools.drlFileLocation
		String droolsRuleDomainClass = config.grails.plugin.drools.droolsRuleDomainClass
		def sourceDir = new File(drlFileLocation)
		println "TEST configurationType: $configurationType"
		println "TEST drlFileLocation: $drlFileLocation"
		println "TEST droolsRuleDomainClass: $droolsRuleDomainClass"
		println "TEST sourceDir: $sourceDir.canonicalPath"

		if (configurationType == "droolsConfigGroovy") {
			//writeDroolsContentXml(basedir, isPluginProject)
		}
	}

	private static Boolean listenerTypeCheck(type) {
		["agendaEventListener", "processEventListener", "ruleRuntimeEventListener"].contains(type)
	}

	private copyFiles(String destination) {
		String drlFileLocationPath = new File("$basedir/$drlFileLocation").canonicalPath
		sourceDir.traverse(type: FILES) {
			String filePath = new File(it.path).canonicalPath
			String newName = ("rules$filePath" - drlFileLocationPath).replaceAll("/", ".").replaceAll("\\\\", ".")
			def newFile = new File(destination, newName)
			newFile.parentFile.mkdirs()
			newFile.write(it.text)
		}
	}

	def writeDroolsContentXml() {
		//println "TEST: " + project.name
		//println "TEST: " + projectDir
		def droolsConfig
		def droolsConfigFile = new File("$projectDir/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
		def droolsContextXmlFile = new File("$projectDir/grails-app/conf/drools-context.xml")
		//def isPluginProject = false
		def slurper = new ConfigSlurper(Environment.current.name)
		//def slurper = new ConfigSlurper()
		/*
		// TODO
		if (project.name == "grails3-drools") {
			isPluginProject = true
		}
		if (isPluginProject) {
			droolsConfigFile = new File("$projectDir/grails-app/conf/DroolsTestConfig.groovy").toURI().toURL()
		} else {
			droolsConfigFile = new File("$projectDir/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
			if (!droolsConfigFile) {
				// TODO
				// Ignore and rely on try-catch below?
				// CreateDroolsContext() ?
				//copyDroolsConfig()
				//droolsConfigFile = new File("$projectDir/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
			}
		}
	*/
		try {
			droolsConfig = slurper.parse(droolsConfigFile)
		}
		catch (e) {
			//if (isPluginProject) {
			//	println "ERROR: $e"
			//} else {
			println "ERROR: grails-app/conf/DroolsConfig.groovy does not exist. Run 'grails create-drools-config'."
			//}
			return
		}
		def writer = new StringWriter()
		def droolsContentXml = new MarkupBuilder(writer)
		droolsContentXml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
		droolsContentXml.beans(xmlns: "http://www.springframework.org/schema/beans",
			"xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
			"xmlns:kie": "http://drools.org/schema/kie-spring",
			"xsi:schemaLocation": "http://www.springframework.org/schema/beans " +
				"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd " +
				"http://drools.org/schema/kie-spring http://drools.org/schema/kie-spring.xsd") {
			"kie:kmodule"(id: "defaultKieModule") {
				droolsConfig.kieBases.each { kieBase ->
					if (!kieBase.includeInConfig) return
					"kie:kbase"(kieBase.attributes) {
						kieBase.kieSessions.each { kieSession ->
							if (!kieSession.includeInConfig) return
							"kie:ksession"(kieSession.attributes) {
								kieSession.kieListeners.each { kieListener ->
									if (!kieListener.includeInConfig) return
									if (!listenerTypeCheck(kieListener.type)) return
									if (kieListener.debug) {
										"kie:$kieListener.type"()
									}
									if (!kieListener.debug && kieListener.ref && !kieListener.nestedBeanClass) {
										"kie:$kieListener.type"(ref: kieListener.ref)
									}
									if (!kieListener.debug && !kieListener.ref && kieListener.nestedBeanClass) {
										"kie:$kieListener.type"() {
											bean(class: kieListener.nestedBeanClass)
										}
									}
								}
							}
						}
					}
				}
			}
			droolsConfig.kieEventListeners.each { listener ->
				if (!listener.includeInConfig) return
				bean(listener.attributes)
			}
			droolsConfig.kieEventListenerGroups.each { group ->
				if (!group.includeInConfig) return
				"kie:eventListeners"(id: group.id) {
					group.listeners.each { listener ->
						if (!listener.includeInConfig) return
						if (!listenerTypeCheck(listener.type)) return
						"kie:$listener.type"(ref: listener.ref)
					}
				}
			}
			bean(id: "kiePostProcessor", class: "org.kie.spring.KModuleBeanFactoryPostProcessor")
		}
		droolsContextXmlFile.write writer.toString()
	}
}
