description("Creates a default drools-context.xml in grails-app/conf/.") {
	usage "grails  create-drools-context"
	flag name:'force', description:"Whether to overwrite existing files"
}

def overwrite = flag("force") ? true : false

render template: 
	template("conf/drools-context.xml"),
	destination: file("grails-app/conf/drools-context.xml"), 
	overwrite: overwrite
