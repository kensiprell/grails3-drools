<%=packageName ? "package ${packageName}\n\n" : ''%>

import grails.test.mixin.*
import spock.lang.*

@TestFor(${className})
@Mock(${className})
class ${className}Spec extends Specification {

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.${modelName}List
            model.${modelName}Count == 0
    }
}