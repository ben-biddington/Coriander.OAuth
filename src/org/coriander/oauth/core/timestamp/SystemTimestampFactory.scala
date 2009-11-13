package org.coriander.oauth.core.timestamp

class SystemTimestampFactory extends TimestampFactory {
    def createTimestamp () : String = (System.currentTimeMillis / 1000) toString
}