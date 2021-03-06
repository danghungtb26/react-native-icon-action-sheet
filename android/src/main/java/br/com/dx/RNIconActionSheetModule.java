
package br.com.dx;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import com.facebook.react.views.text.ReactFontManager;

import java.net.URL;

public class RNIconActionSheetModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private static final String ANDROID_RESOURCE_SCHEME = "android.resource";

    public RNIconActionSheetModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNIconActionSheet";
    }

    @ReactMethod
    public void showActionSheetWithOptions(final ReadableMap props, final Callback callback) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getCurrentActivity(), R.style.RNIconActionSheet_DialogStyle);

        LinearLayout sheetView = (LinearLayout) getCurrentActivity().getLayoutInflater().inflate(R.layout.rn_iconactionsheet_list, null);
        bottomSheetDialog.setContentView(sheetView);

        if (props.hasKey("title") && !props.isNull("title")) {
            String title = props.getString("title");
            TextView headerView = (TextView) getCurrentActivity().getLayoutInflater().inflate(R.layout.rn_iconactionsheet_list_header, null);
            headerView.setText(title);
            sheetView.addView(headerView);
        }

        ReadableArray options = props.getArray("options");

        for ( int index = 0; index < options.size(); index++) {

            ReadableMap option = options.getMap(index);
            LinearLayout itemView = (LinearLayout) getCurrentActivity().getLayoutInflater().inflate(R.layout.rn_iconactionsheet_list_item, null);

            if (option.hasKey("title") && !option.isNull("title")) {
                final String title = option.getString("title");
                TextView titleTextView = itemView.findViewById(R.id.textView);
                titleTextView.setText(title);
                final int in = index;
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        callback.invoke(in);
                        bottomSheetDialog.dismiss();
                    }
                });
            }

            if (option.hasKey("type") && !option.isNull("type")) {
                int type = option.getInt("type");
                if (type == 2) {
                    ReadableMap itemIcon = option.getMap("icon");
                    if (itemIcon != null) {
                        String item = itemIcon.getString("uri");
                        if(item != null) {
                            ImageView itemImageView = itemView.findViewById(R.id.imageView);



                            if(startsWithValidScheme(item)) {
                                Bitmap drawable = this.getBitmapImage(item);
                                itemImageView.setImageBitmap(drawable);
                            } else {
                                try {
                                    Drawable drawable = this.generateImage(item);
                                    itemImageView.setImageDrawable(drawable);
                                }
                                catch (Exception e) {

                                }
                            }



                        }

                    }
                } else if (type == 3) {
                    ReadableMap itemIcon = option.getMap("icon");
                    if (itemIcon != null) {
                        ImageView itemImageView = itemView.findViewById(R.id.imageView);
                        Drawable drawable = this.generateVectorIcon(itemIcon);
                        itemImageView.setImageDrawable(drawable);
                    }
                }

            }

            sheetView.addView(itemView);
        }



        bottomSheetDialog.show();
    }

    private Bitmap getBitmapImage(String url) {
        Bitmap mIcon_val = null;
        try {

            URL newurl = new URL(url);
            mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return mIcon_val;
        }

    }

    private boolean startsWithValidScheme(String uriString) {
        return uriString.startsWith("http://")
                || uriString.startsWith("https://")
                || uriString.startsWith("content://")
                || uriString.startsWith("file://")
                || uriString.startsWith("asset://");
    }

    private Drawable generateImage(String name) {

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);


		Resources resources = getReactApplicationContext().getResources();

		int resourceId = resources.getIdentifier(name, "drawable", getReactApplicationContext().getPackageName());

		if(resourceId == 0) {
            resourceId = resources.getIdentifier(name, "res", getReactApplicationContext().getPackageName());

        }


		if(resourceId > 0)

		return getReactApplicationContext().getDrawable(resourceId);

        return null;
    }

    @TargetApi(21)
    private Drawable generateVectorIcon(ReadableMap icon) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String family = icon.getString("family");
        String glyph = icon.getString("glyph");
        Double color = icon.getDouble("color");
        int size = icon.getInt("size");

        float scale = getReactApplicationContext().getResources().getDisplayMetrics().density;
        int fontSize = Math.round(size * scale);

        Typeface typeface = ReactFontManager.getInstance().getTypeface(family, 0, getReactApplicationContext().getAssets());
        Paint paint = new Paint();
        paint.setTypeface(typeface);
        paint.setColor(color.intValue());
        paint.setTextSize(fontSize);
        paint.setAntiAlias(true);
        Rect textBounds = new Rect();
        paint.getTextBounds(glyph, 0, glyph.length(), textBounds);

        Bitmap bitmap = Bitmap.createBitmap(textBounds.width(), textBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(glyph, -textBounds.left, -textBounds.top, paint);

        return new BitmapDrawable(getReactApplicationContext().getResources(), bitmap);
    }
}
