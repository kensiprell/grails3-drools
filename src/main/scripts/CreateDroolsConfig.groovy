description("Creates a default DroolsConfig.groovy in grails-app/conf/.") {
    usage "grails create-drools-config"
}

render  template:"DroolsConfig.groovy",
        destination: file( "grails-app/conf/DroolsConfig.groovy")

		// TODO warn if overwrite
/*

description "Installs scaffolding templates that use f:all to render properties", "grails install-form-fields-templates"

updateStatus "Copying fields templates"
copy {
	from templates("fields/scaffolding*/
/*.gsp")
	into "src/templates/scaffolding"
}
addStatus "Template installation complete"
*/


/*
includeTargets << new File(droolsPluginDir, "scripts/_DroolsUtils.groovy")

target(createDroolsConfig: "Creates a default DroolsConfig.groovy in grails-app/conf/.") {
	def droolsConfigFile = new File(basedir, "grails-app/conf/DroolsConfig.groovy")
	if (!droolsConfigFile.exists()) {
		copyDroolsConfig()
		println "Created default DroolsConfig.groovy in grails-app/conf/."
	} else {
		println "Error: grails-app/conf/DroolsConfig.groovy exists and was not overwritten."
	}
}

USAGE = """
    create-drools-config
"""

setDefaultTarget 'createDroolsConfig'
*/
