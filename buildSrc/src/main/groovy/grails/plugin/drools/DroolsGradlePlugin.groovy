package grails.plugin.drools

import groovy.xml.MarkupBuilder
import org.gradle.api.file.FileTree
import org.gradle.api.Project
import org.gradle.api.Plugin

class DroolsGradlePlugin implements Plugin<Project> {
	final DROOLS_VERSION = "6.2.0.Final"
	final COM_SUN_XML_BIND_VERSION = "2.2.11"
	final JANINO_VERSION = "2.7.5"
	final XSTREAM_VERSION = "1.4.7"
	final ECJ_VERSION = "4.4"
	final MVEL_VERSION = "2.2.2.Final"
	final ANTLR_RUNTIME_VERSION = "3.5.2"

	private static Boolean listenerTypeCheck(type) {
		["agendaEventListener", "processEventListener", "ruleRuntimeEventListener"].contains(type)
	}

	void apply(Project project) {
		project.task("getDroolsPluginCompile") {
			String droolsVersion = project.hasProperty("droolsVersion") ? project.droolsVersion : DROOLS_VERSION
			project.ext.droolsPluginCompile = [
					"org.drools:drools-compiler:$droolsVersion",
					"org.drools:drools-core:$droolsVersion",
					"org.drools:drools-decisiontables:$droolsVersion",
					"org.drools:drools-jsr94:$droolsVersion",
					"org.drools:drools-verifier:$droolsVersion",
					"org.kie:kie-api:$droolsVersion",
					"org.kie:kie-internal:$droolsVersion",
					"org.kie:kie-spring:$droolsVersion"
				]
		}

		project.task("getDroolsPluginRuntime") {
			String comSunXmlBindVersion = project.hasProperty("comSunXmlBindVersion") ? project.comSunXmlBindVersion : COM_SUN_XML_BIND_VERSION
			String janinoVersion = project.hasProperty("janinoVersion") ? project.janinoVersion : JANINO_VERSION
			String xstreamVersion = project.hasProperty("xstreamVersion") ? project.xstreamVersion : XSTREAM_VERSION
			String ecjVersion = project.hasProperty("ecjVersion") ? project.ecjVersion : ECJ_VERSION
			String mvelVersion = project.hasProperty("mvelVersion") ? project.mvelVersion : MVEL_VERSION
			String antlrRuntimeVersion = project.hasProperty("antlrRuntimeVersion") ? project.antlrRuntimeVersion : ANTLR_RUNTIME_VERSION
			project.ext.droolsPluginRuntime = [
				"com.sun.xml.bind:jaxb-xjc:$comSunXmlBindVersion",
				"com.sun.xml.bind:jaxb-impl:$comSunXmlBindVersion",
				"org.codehaus.janino:janino:$janinoVersion",
				"com.thoughtworks.xstream:xstream:$xstreamVersion",
				"org.eclipse.jdt.core.compiler:ecj:$ecjVersion",
				"org.mvel:mvel2:$mvelVersion",
				"org.antlr:antlr-runtime:$antlrRuntimeVersion"
			]
		}

		project.task('copyDroolsRule') {
			FileTree tree
			String destination
			String drlFileLocationPath
			String droolsDrlFileLocation = project.droolsDrlFileLocation ?: "src/rules"
			project.gradle.taskGraph.whenReady { graph ->
				destination = "$project.buildDir/classes/main"
				// TODO ? add test and path for war creation "$stagingDir/WEB-INF/classes"
				drlFileLocationPath = new File("$project.projectDir/$droolsDrlFileLocation").canonicalPath
				tree = project.fileTree(drlFileLocationPath) {
					include "**/*.drl"
					include "**/*.rule"
				}
				inputs.file drlFileLocationPath
				outputs.dir destination
			}
			doLast {
				// allow kie:spring to find packages
				def directory = droolsDrlFileLocation.tokenize('/')[-1]
				project.copy {
					from drlFileLocationPath.toString()
					include "**/*.drl"
					include "**/*.rule"
					into "$destination/$directory"
				}
				// violates DRY
				// allows classLoader.getResourceAsStream("rules.application.application.drl")
				// otherwise use classLoader.getResourceAsStream("rules/application/application.drl")
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
						"http://www.springframework.org/schema/beans/spring-beans-4.0.xsd " +
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