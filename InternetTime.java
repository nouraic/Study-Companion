package com.oneplusplus.christopher.studycompanion;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class InternetTime {
    private final String TIME_SERVER = "time-a.timefreq.bldrdoc.gov";
    private final int DEFAULT_TIMEOUT = 1000;
    private final int MAX_CONNECT_TRIES = 10;

    private int numAttempts;    //number of connection attempts

    public InternetTime() {

    }

    //Connects to a time server to get the current internet time
    public Date fetchCurrentInternetTime() {
        NTPUDPClient timeClient = new NTPUDPClient();
        timeClient.setDefaultTimeout(DEFAULT_TIMEOUT);

        for (int i = 0; i < MAX_CONNECT_TRIES; i++) {
            numAttempts = 0;
            try {
                //Try connecting to the network to get the time
                InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();

                //convert long to date and date to String
                Date networkTimeDate = new Date(returnTime);

                numAttempts = i + 1;
                return networkTimeDate;
            } catch (IOException e) {

            }
        }
        return null;
    }

    //returns the number of attempts it took to reach the server usefull for testing
    //reliability
    public int getNumAttempts() {
        return numAttempts;
    }
}
