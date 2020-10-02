package com.cz.android.gif.sample;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by cz
 * @date 2020/9/25 4:16 PM
 * @email bingo110@126.com
 */
public class TimeTrace {
    private Map<String,Long> timeRecord=new LinkedHashMap<>();
    private TimeUnit timeUnit;
    private long currentTime;

    public TimeTrace(){
        this(TimeUnit.MICROSECONDS);
    }

    public TimeTrace(TimeUnit timeUnit){
        this.timeUnit = timeUnit;
        this.currentTime =System.nanoTime();
    }

    public void trace(String tag){
        long nanoTime = System.nanoTime();
        if(TimeUnit.MILLISECONDS == timeUnit){
            timeRecord.put(tag,(nanoTime - currentTime)/1000_1000L);
        } else if(TimeUnit.MICROSECONDS == timeUnit){
            timeRecord.put(tag,(nanoTime- currentTime)/1000L);
        } else if(TimeUnit.NANOSECONDS == timeUnit){
            timeRecord.put(tag,nanoTime - currentTime);
        }
        currentTime = nanoTime;
    }

    public int size(){
        return timeRecord.size();
    }

    public Map<String, Long> getTimeRecord() {
        return timeRecord;
    }

    @Override
    public String toString() {
        StringBuilder output=new StringBuilder();
        for(Map.Entry<String,Long> entry:timeRecord.entrySet()){
            output.append(entry.getKey()+" time:"+entry.getValue()+"\n");
        }
        return output.toString();
    }
}
