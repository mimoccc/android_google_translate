package com.bgh;

/**
 * User: asuka
 * Date: 11-6-22
 * Time: 下午10:35
 */
public class TranslateTask implements Runnable {

    private static final String TAG = "TranslateTask";
    private final Translate translate;
    private final String origin, from, to;

    public TranslateTask(Translate translate, String original, String from, String to) {
        this.translate = translate;
        this.origin = original;
        this.from = from;
        this.to = to;
    }

    public void run() {

    }

}
