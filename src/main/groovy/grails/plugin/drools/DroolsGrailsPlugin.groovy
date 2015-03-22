package grails.plugin.drools

import grails.plugins.Plugin

class DroolsGrailsPlugin extends Plugin {

	def grailsVersion = "3.0 > *"
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
	def issueManagement = [system: "github", url: "https://github.com/kensiprell/grails3-drools/issues"]
	def scm = [url: "https://github.com/kensiprell/grails-drools"]
	def droolsDrlFileLocation = getDroolsDrlFileLocation()
	def pluginExcludes = [
		"grails-app/conf/drools-context.xml",
		"grails-app/conf/DroolsConfig.groovy",
		"$droolsDrlFileLocation/**"
	]
	def watchedResources = [
		"file:./grails-app/conf/DroolsConfig.groovy",
		"file:./grails-app/conf/drools-context.xml",
		"file:./$droolsDrlFileLocation/**/*.drl",
		"file:./$droolsDrlFileLocation/**/*.rule"
	]

	Closure doWithSpring() {
		{ ->
			try {
				importBeans("drools-context.xml")
			} catch (e) {
				log.debug(e)
				log.error "grails-app/conf/drools-context.xml does not exist. Try 'grails create-drools-config' or 'grails create-drools-context'."
			}
		}
	}

	@Override
	void onChange(Map<String, Object> event) {
		// event.source, event.application, event.manager, event.ctx, and event.plugin.
		def name = event.source.name
		println "TEST event.source.name: $event.source.name"

		// Change in DroolsConfig.groovy
		if (name == "DroolsConfig") {
			println "\nChanges to DroolsConfig.groovy will be implemented when the application is restarted.\n"
			// TODO? writeDroolsContentXml() and importBeans("drools-context.xml")
		}

		// Change in drools-context.xml
		if (name == "drools-context.xml") {
			println "\nChanges to drools-context.xml will be implemented when the application is restarted.\n"
			// TODO? importBeans("drools-context.xml")
		}

		// Change in a Drools rule
		if (name.endsWith("drl") || name.endsWith("rule")) {
			// TODO
			String currentDir = new File(".").toString()
			String ruleDestinationBaseDirectory = droolsDrlFileLocation.tokenize('/')[-1]
			def destination = new File("$currentDir/build/classes/main/$ruleDestinationBaseDirectory").canonicalPath
			println "TEST destination: $destination"
			// copy event.source? to "$destination"
			// String drlFileLocationPath = new File("$project.projectDir/$droolsDrlFileLocation").canonicalPath
			// String filePath = event.source.canonicalPath
			// String newName = ("$ruleDestinationBaseDirectory/$filePath" - drlFileLocationPath).replaceAll("/", ".").replaceAll("\\\\", ".")
			// copy event.source? to "$destination/$newName"
		}
	}

	private static String getDroolsDrlFileLocation() {
		Properties properties = new Properties()
		File propertiesFile = new File("gradle.properties")
		if (propertiesFile) {
			propertiesFile.withInputStream {
				properties.load(it)
			}
		}
		properties.droolsDrlFileLocation ?: "src/rules"
	}
}
