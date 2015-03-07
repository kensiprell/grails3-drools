package grails.plugin.drools.test

class Ticket {
	Customer customer
	String status = 'New'

/*
	Ticket(Long id, Customer customer) {
		this()
		this.id = id
		this.customer = customer
		status = 'New'
	}
*/

	String toString() {
		"Ticket #$id: Customer[$customer] Status[$status]"
	}
}
