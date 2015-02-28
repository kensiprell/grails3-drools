/*
import org.grails.cli.interactive.completers.DomainClassCompleter
import org.springframework.beans.factory.annotation.*

description("Creates a domain class for drools plugin rules and updates Config.groovy.") {
	usage "grails create-drools-domain [DOMAIN CLASS]"
	completer DomainClassCompleter
	flag name:'force', description:"Whether to overwrite existing files"
}

if(args) {
	def sourceClass = source(args[0])
	def overwrite = flag('force') ? true : false
	if(!sourceClass) {
		def model = model(args[0])
		def className = model.convention("Domain")
		render template: template("drools/DroolsDomainClass.groovy"), 
			destination: file("grails-app/domain/${model.packagePath}/$className.groovy"),
			model: model,
			overwrite: overwrite
		render template: template("drools/Spec.groovy"), 
			destination: file("src/test/groovy/${model.packagePath}/${model.convention('DoamainSpec')}.groovy"),
			model: model,
			overwrite: overwrite
		addStatus "Domain class ${projectPath(sourceClass)} added."           

		@Value('${grails.plugin.drools.droolsRuleDomainClass}')
		String config
		def configFile = file("$baseDir/grails-app/conf/application.yml")
		String newConfig
		String Status
		if (!config) {
			newConfig = """
// Added by the Drools plugin:
grails:
    plugin:
        drools:
            droolsRuleDomainClass: '$className'
"""
			configFile.append(newConfig)
			status = "Please verify your grails-app/conf/application.yml was updated:\n$newConfig"
		} else if (config != className) {
			def configFileText = configFile.text
			def replacementText = "droolsRuleDomainClass: '${className}' // Changed by Drools plugin"
			configFileText = configGroovyText.replaceAll(/(?m)droolsRuleDomainClass.*$/, replacementText)
			configFile.write(configGroovyText)
			status = "Please verify your grails-app/conf/application.yml was updated:\n$replacementText"
		}
		if (newConfig) {
			addStatus status
		}
	} else {
		error "Domain class not found for name $arg"
	}
} else {
	error "No domain class specified"
}
*/
