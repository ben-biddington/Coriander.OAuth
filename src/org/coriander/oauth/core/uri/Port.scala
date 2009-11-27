package org.coriander.oauth.core.uri

class Port (val scheme : String, val number : java.lang.Integer)
object Port {
	def apply(scheme : String, number : java.lang.Integer) : Port = new Port(scheme, number)
}