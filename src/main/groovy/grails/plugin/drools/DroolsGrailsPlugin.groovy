package grails.plugin.drools

import grails.plugins.Plugin

class DroolsGrailsPlugin extends Plugin {

	def grailsVersion = "3.0 > *"
	def pluginExcludes = [
		"grails-app/conf/drools-context.xml",
		"grails-app/conf/DroolsConfig.groovy",
		"grails-app/domain/**",
		"src/rules/**"
	]
	def title = "Drools Plugin"
	def author = "Ken Siprell"
	def authorEmail = "ken.siprell@gmail.com"
	def developers = [
		[name: "Burt Beckwith", email: "burt@burtbeckwith.com"]
	]
	def description = "This plugin integrates the [Drools|https://www.drools.org] Business Rules Management System."
	def profiles = ['web']
	def documentation = "https://github.com/kensiprell/grails-drools/blob/master/README.md"
	def license = "APACHE"
	def issueManagement = [system: "JIRA", url: "https://jira.grails.org/browse/GPDROOLS"]
	def scm = [url: "https://github.com/kensiprell/grails-drools"]

	Closure doWithSpring() {
		{ ->
			println "PLUGIN: doWithSpring"
			try {
				importBeans("drools-context.xml")
			} catch (e) {
				log.debug(e)
				log.error "grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
			}
		}
	}

	void doWithDynamicMethods() {
		// TODO Implement registering dynamic methods to classes (optional)
		println "PLUGIN: doWithDynamicMethods"
	}

	void doWithApplicationContext() {
		// TODO Implement post initialization spring config (optional)
		println "PLUGIN: doWithApplicationContext"
		//println "TEST: " + config.flatten().each { println it }
		//ProcessDroolsResources.process(config)
	}

	void onChange(Map<String, Object> event) {
		// TODO Implement code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
		println "PLUGIN: onChange $event"
	}

	void onConfigChange(Map<String, Object> event) {
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
		println "PLUGIN: onConfigChange $event"
	}

	void onShutdown(Map<String, Object> event) {
		// TODO Implement code that is executed when the application shuts down (optional)
		println "PLUGIN: onShutdown $event"
	}
}
