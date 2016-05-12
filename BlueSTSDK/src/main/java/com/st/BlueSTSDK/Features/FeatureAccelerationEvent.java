/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/

package com.st.BlueSTSDK.Features;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * This feature will contain the different event that can be discovered using an accelerometer data.
 * The events can be generated by different algorithms that must be enabled using the function
 * {@link FeatureAccelerationEvent#detectEvent(DetectableEvent, boolean)}.
 * When an algorithm is enable the user can receive a notification thought the
 * {@link FeatureAccelerationEvent.FeatureAccelerationEventListener#onDetectableEventChange(com.st.BlueSTSDK.Features.FeatureAccelerationEvent, com.st.BlueSTSDK.Features.FeatureAccelerationEvent.DetectableEvent, boolean)}
 *  function.
 * <p>
 *     Note: Only one algorithm at time is supported.
 * </p>
 * <p>
 *     When the pedometer is enabled the you can use {@link
 *     FeatureAccelerationEvent#getPedometerSteps(com.st.BlueSTSDK.Feature.Sample)}
 *     for retrieve the number of steps. otherwise the function will return a negative number
 * </p>
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureAccelerationEvent extends Feature {

    public static final String FEATURE_NAME = "AccelerationEvent";
    public static final String FEATURE_UNIT = "";
    public static final String FEATURE_DATA_NAME[] ={"Event","nSteps"} ;
    public static final Number DATA_MAX[] = {256, Short.MAX_VALUE};
    public static final Number DATA_MIN[] = {0,0};

    private static final int ACC_EVENT = 0;
    private static final int PEDOMETER_DATA = 1;
    /**
     * fake value used for notify a pedometer event
     */
    private static final short PEDOMETER_EVENT_ENUM_VALUE =0x100;

    /**
     * command used for disable the a specific event notification
     */
    private static final byte COMMAND_DISABLE[] = {0};

    /**
     * command used for enable the a specific event notification
     */
    private static final byte COMMAND_ENABLE[] = {1};

    /**
     * event that can be fired by the accelerometer
     */
    public enum AccelerationEvent{
        NO_EVENT,
        ORIENTATION_TOP_LEFT,
        ORIENTATION_TOP_RIGHT,
        ORIENTATION_BOTTOM_LEFT,
        ORIENTATION_BOTTOM_RIGHT,
        ORIENTATION_UP,
        ORIENTATION_DOWN,
        TILT,
        FREE_FALL,
        SINGLE_TAP,
        DOUBLE_TAP,
        WAKE_UP,
        PEDOMETER,
        ERROR
    }//AccelerationEvent

    /**
     * algorithm that can run in the accelerometer
     */
    public enum DetectableEvent {
        NONE((byte)'\0'),
        ORIENTATION((byte)'o'),
        FREE_FALL((byte)'f'),
        SINGLE_TAP((byte)'s'),
        DOUBLE_TAP((byte)'d'),
        WAKE_UP((byte)'w'),
        PEDOMETER((byte)'p'),
        TILT((byte)'t');

        /**
         * byte used as command type when we enable/disable the algorithm
         */
        private byte typeId;

        DetectableEvent(byte b){
            typeId=b;
        }

        byte getTypeId() {
            return typeId;
        }

        /**
         * create DetectableEvent starting from the byte value
         * @param b command type byte
         * @return null if is not a value command type or the algorithm that generate the command
         */
        @Nullable
        static DetectableEvent build(byte b){
            for (DetectableEvent event: DetectableEvent.values() ) {
                if(event.getTypeId()==b)
                    return  event;
            }//for
            return  null;
        }//build
    }//DetectableEvent

    /**
     * event that is currently detected by the accelerometer or null if nothing is enabled
     */
    private DetectableEvent mEnabledEvent = DetectableEvent.NONE;

    /**
     * extract the Event from a sensor sample
     * @param sample data read from the node
     * @return type of event detected by the node
     */
    public static AccelerationEvent getAccelerationEvent(Sample sample){
        if(sample!=null)
            if(sample.data.length>ACC_EVENT)
                if (sample.data[ACC_EVENT] != null){
                    short event = sample.data[ACC_EVENT].shortValue();
                    switch (event){
                        case 0x00:
                            return AccelerationEvent.NO_EVENT;
                        case 0x01:
                            return AccelerationEvent.ORIENTATION_TOP_RIGHT;
                        case 0x02:
                            return AccelerationEvent.ORIENTATION_BOTTOM_RIGHT;
                        case 0x03:
                            return AccelerationEvent.ORIENTATION_BOTTOM_LEFT;
                        case 0x04:
                            return AccelerationEvent.ORIENTATION_TOP_LEFT;
                        case 0x05:
                            return AccelerationEvent.ORIENTATION_UP;
                        case 0x06:
                            return AccelerationEvent.ORIENTATION_DOWN;
                        case 0x08:
                            return AccelerationEvent.TILT;
                        case 0x10:
                            return AccelerationEvent.FREE_FALL;
                        case 0x20:
                            return AccelerationEvent.SINGLE_TAP;
                        case 0x40:
                            return AccelerationEvent.DOUBLE_TAP;
                        case 0x80:
                            return AccelerationEvent.WAKE_UP;
                        case PEDOMETER_EVENT_ENUM_VALUE:
                            return AccelerationEvent.PEDOMETER;
                        default:
                            return AccelerationEvent.ERROR;
                    }//switch */
                }//if
            //if
        //if sample!=null
        return AccelerationEvent.ERROR;
    }//getActivityStatus


    /**
     * if you are running the pedometer, calling this function you can read the number of steps done
     * @param sample sample to read
     * @return number of steps or a negative number if the pedometer isn't enabled
     */
    public static int getPedometerSteps(Sample sample){
        if(sample!=null)
            if(sample.data.length>PEDOMETER_DATA)
                if (sample.data[PEDOMETER_DATA] != null &&
                        sample.data[ACC_EVENT].shortValue()==PEDOMETER_EVENT_ENUM_VALUE)
                    return sample.data[PEDOMETER_DATA].intValue();
        return -1;
    }//getPedometerSteps

    /**
     * build a activity feature
     * @param n node that will send data to this feature
     */
    public FeatureAccelerationEvent(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[ACC_EVENT], FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX[ACC_EVENT],DATA_MIN[ACC_EVENT]),
                new Field(FEATURE_DATA_NAME[PEDOMETER_DATA], FEATURE_UNIT, Field.Type.UInt16,
                        DATA_MAX[PEDOMETER_DATA],DATA_MIN[PEDOMETER_DATA])
        });
    }//FeatureActivity

    /**
     * read a byte with the event data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the Activity id)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if(mEnabledEvent == DetectableEvent.PEDOMETER) {
            if (data.length - dataOffset < 2)
                throw new IllegalArgumentException("There are no 2 byte available to read");
            Sample temp = new Sample(timestamp,new Number[]{
                    PEDOMETER_EVENT_ENUM_VALUE,
                    NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset)
            });
            return new ExtractResult(temp,2);
        }else {
            if (data.length - dataOffset < 1)
                throw new IllegalArgumentException("There are no 1 byte available to read");
            Sample temp = new Sample(timestamp, new Number[]{
                    NumberConversion.byteToUInt8(data, dataOffset)
            });
            return new ExtractResult(temp, 1);
        }//if-else
    }//extractData

    /**
     * start looking for an event type
     * @param event algorithm to enable/disable
     * @param enable true for enable it, false for disable it
     * @return true if the command is correctly send or the algorithm is already enabled
     * <p>
     *     Only one algorithm can run at time, enabling an algorithm will disable the previous one.
     * </p>
     */
    public boolean detectEvent(DetectableEvent event, boolean enable){

        if(enable && event != mEnabledEvent && mEnabledEvent!=DetectableEvent.NONE)
            sendCommand(mEnabledEvent.getTypeId(),COMMAND_DISABLE); //disable the previous one

        if(event != DetectableEvent.NONE)
            return sendCommand(event.getTypeId(), enable ? COMMAND_ENABLE : COMMAND_DISABLE);
        else{
            notifyEventEnabled(DetectableEvent.NONE,true);
            return true;
        }
    }

    /**
     * @return the algorithm that is running or null if no algorithm are running
     */
    public @Nullable
    DetectableEvent getEnabledEvent(){
        return mEnabledEvent;
    }

    /**
     * notify to all the listener of type FeatureAccelerationEventListener that the status
     * of an algorithm is changed
     * @param event event that change
     * @param status new status
     */
    protected void notifyEventEnabled(final DetectableEvent event,final boolean status) {
        for (final FeatureListener listener : mFeatureListener) {
            if (listener instanceof FeatureAccelerationEventListener)
                sThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((FeatureAccelerationEventListener) listener)
                                .onDetectableEventChange(FeatureAccelerationEvent.this, event, status);
                    }//run
                });
        }//for
    }//notifyUpdate

    /**
     *
     * @param timeStamp device time stamp of when the response was send
     * @param commandType id of the request that the feature did
     * @param data data attached to the response
     */
    @Override
    protected void parseCommandResponse(int timeStamp, byte commandType, byte[] data) {

        DetectableEvent event= DetectableEvent.build(commandType);
        if(event==null)
            return;

        boolean status = data[0]==1;

        if(status){
            mEnabledEvent=event;
        }else if(mEnabledEvent==event){
            mEnabledEvent=DetectableEvent.NONE;
        }//if-else-if

        notifyEventEnabled(event,status);
    }

    @Override
    public String toString(){
        Sample sample = mLastSample;
        if(sample!=null){
            AccelerationEvent event = getAccelerationEvent(sample);
            String pedometerData="";
            if(event==AccelerationEvent.PEDOMETER)
                pedometerData = "\n\tnSteps: "+getPedometerSteps(sample);
            return FEATURE_NAME+":\n"+
                    "\tTimestamp:"+ sample.timestamp+"\n" +
                    "\tEvent: "+ event.name() +
                    pedometerData;

        }else{
            return super.toString();
        }//if-else

    }//toString

    /**
     * Extend the FeatureListener for notify also the enabling/disabling of a new algorithm
     */
    public interface FeatureAccelerationEventListener extends Feature.FeatureListener {

        /**
         * An event detection algorithm is enable or disabled
         *
         * @param f feature where the event change
         * @param event event that has change its status
         * @param newStatus true if the event is enabled, false otherwise
         */
        void onDetectableEventChange(FeatureAccelerationEvent f, DetectableEvent event, boolean newStatus);
    }
}
