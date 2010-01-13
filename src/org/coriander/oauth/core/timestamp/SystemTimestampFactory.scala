package org.coriander.oauth.core.timestamp

class SystemTimestampFactory extends TimestampFactory {
    def newTimestamp () = (System.currentTimeMillis / 1000) toString
}