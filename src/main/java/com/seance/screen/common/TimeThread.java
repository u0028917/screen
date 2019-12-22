package com.seance.screen.common;

import javax.mail.Part;

public class TimeThread extends Thread {
    private Part part;

    void TimeThread(Part part){
        this.part = part;
    }


    @Override
    public void run() {

    }
}
