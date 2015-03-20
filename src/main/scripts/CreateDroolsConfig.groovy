description("Creates a default DroolsConfig.groovy in grails-app/conf/.") {
	usage "grails create-drools-config"
	flag name:'force', description:"Whether to overwrite existing files"
}

def overwrite = flag("force") ? true : false

render template: 
	template("conf/DroolsConfig.groovy"),
	destination: file("grails-app/conf/DroolsConfig.groovy"), 
	overwrite: overwrite
