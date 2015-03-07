package grails.plugin.drools

import groovy.xml.MarkupBuilder
import org.gradle.api.file.FileTree
import org.gradle.api.Project
import org.gradle.api.Plugin

class DroolsGradlePlugin implements Plugin<Project> {

	private static Boolean listenerTypeCheck(type) {
		["agendaEventListener", "processEventListener", "ruleRuntimeEventListener"].contains(type)
	}

	void apply(Project project) {
		project.task('copyDroolsRule') {
			FileTree tree
			String destination
			String drlFileLocationPath
			String droolsDrlFileLocation = project.droolsDrlFileLocation ?: "src/rules"
			project.gradle.taskGraph.whenReady { graph ->
				destination = "$project.buildDir/resources/main"
				// TODO ? add test and path for war creation "$stagingDir/WEB-INF/classes"
				drlFileLocationPath = new File("$project.projectDir/$droolsDrlFileLocation").canonicalPath
				 tree = project.fileTree(drlFileLocationPath) {
					include "**/*.drl"
					include "**/*.rule"
				}
				inputs.file tree
				outputs.dir destination
			}
			doLast {
				tree.each { File file ->
					String filePath = file.canonicalPath
					String newName = ("rules$filePath" - drlFileLocationPath).replaceAll("/", ".").replaceAll("\\\\", ".")
					project.copy {
						from file
						rename { String fileName ->
							fileName.replace(fileName, newName)
						}
						into destination
					}
				}
			}
		}

		project.task('writeDroolsContentXml') {
			def droolsConfigFile = new File("$project.projectDir/grails-app/conf/DroolsConfig.groovy").toURI().toURL()
			def droolsContextXmlFile = new File("$project.projectDir/grails-app/conf/drools-context.xml")
			inputs.file droolsConfigFile
			outputs.file droolsContextXmlFile
			doLast {
				if (project.drooolsConfigurationType != "droolsConfigGroovy") return
				if (!droolsConfigFile) {
					println "ERROR: grails-app/conf/DroolsConfig.groovy does not exist. Run 'grails create-drools-config'."
					return
				}
				def slurper = new ConfigSlurper()
				def droolsConfig = slurper.parse(droolsConfigFile)
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
	}
}