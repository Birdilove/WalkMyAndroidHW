package com.example.walkmyandroidhw;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressTask extends AsyncTask <Location, Void, String> {
    private OnTaskCompleted onTaskCompleted;
    private Context mContext;

    public FetchAddressTask(Context context, OnTaskCompleted taskCompleted) {
        this.mContext = context;
        this.onTaskCompleted = taskCompleted;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        Location location = locations[0];
        List<Address> addresses = null;
        String resultMessage = "";

        try{
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses == null || addresses.size() == 0){
                if (resultMessage.isEmpty()){
                    resultMessage = mContext.getString(R.string.no_address_found);
                }
            }
            else{
                Address address = addresses.get(0);
                ArrayList<String> addressParts = new ArrayList<>();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    addressParts.add(address.getAddressLine(i));
                }
                resultMessage = TextUtils.join("\n", addressParts);
            }
        }
        catch (IOException e){
            resultMessage = mContext.getString(R.string.service_not_available);
        }
        catch (IllegalArgumentException e){
            resultMessage = mContext.getString(R.string.invalid_lat_long_used);
        }
        return resultMessage;
    }

    @Override
    protected void onPostExecute(String s) {
        onTaskCompleted.onTaskCompleted(s);
        super.onPostExecute(s);
    }

    interface OnTaskCompleted{
        void onTaskCompleted(String result);
    }
}
