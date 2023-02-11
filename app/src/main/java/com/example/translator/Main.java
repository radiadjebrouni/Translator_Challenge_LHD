/**************************************************************
 * DJEBROUNI RADHIA
 * LHD TRANSLATOR CHALLENGE
 *
 * ressources:
 *https://developers.google.com/ml-kit/language/translation/android
 * https://stacktips.com/tutorials/android/speech-to-text-in-android
 * youtub
 * https://github.com/komamitsu/Android-OCRSample
 * stackoverflow
 ****************************************************************/





package com.example.translator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;


import static java.security.AccessController.getContext;

public class Main  extends AppCompatActivity {
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextInputEditText srcTextView = null;

    private ImageButton mSpeakBtn;
    private ImageButton mCamBtn;
    private ImageButton mgall;
    private TextView targetTextView;

    private CameraSource mCameraSource;
    private TextRecognizer mTextRecognizer;
    private SurfaceView mSurfaceView;

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private final int CAMERA_SCAN_TEXT = 0;
    private final int LOAD_IMAGE_RESULTS = 1;

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int MY_PERMISSIONS_REQUESTS = 0;

    private static final String TAG = Main.class.getSimpleName();

    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.translate_fragment );
         srcTextView = findViewById(R.id.sourceText);




        final Button switchButton = findViewById(R.id.buttonSwitchLang);
        final ToggleButton sourceSyncButton = findViewById(R.id.buttonSyncSource);
        final ToggleButton targetSyncButton = findViewById(R.id.buttonSyncTarget);

         targetTextView = findViewById(R.id.targetText);
        final TextView downloadedModelsTextView = findViewById(R.id.downloadedModels);
        final Spinner sourceLangSelector = findViewById(R.id.sourceLangSelector);
        final Spinner targetLangSelector = findViewById(R.id.targetLangSelector);

        final TranslateViewModel viewModel = ViewModelProviders.of(this).get(TranslateViewModel.class);

        // Get available language list and set up source and target language spinners
        // with default selections.
        final ArrayAdapter<TranslateViewModel.Language> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, viewModel.getAvailableLanguages());
        sourceLangSelector.setAdapter(adapter);
        targetLangSelector.setAdapter(adapter);
        sourceLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("en")));
        targetLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("fr")));









        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        TranslateRemoteModel frenchModel =
                new TranslateRemoteModel.Builder( TranslateLanguage.FRENCH).build();
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        modelManager.download(frenchModel, conditions)
                .addOnSuccessListener(new OnSuccessListener () {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i ("sss","succ");
                    }


                })
                .addOnFailureListener(new OnFailureListener () {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i ("sss",e.getMessage ());

                    }



                });




        sourceLangSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setProgressText(targetTextView);
                viewModel.sourceLang.setValue(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                targetTextView.setText("");
            }
        });
        targetLangSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setProgressText(targetTextView);
                viewModel.targetLang.setValue(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                targetTextView.setText("");
            }
        });

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressText(targetTextView);
                int sourceLangPosition = sourceLangSelector.getSelectedItemPosition();
                sourceLangSelector.setSelection(targetLangSelector.getSelectedItemPosition());
                targetLangSelector.setSelection(sourceLangPosition);
            }
        });

        // Set up toggle buttons to delete or download remote models locally.
        sourceSyncButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TranslateViewModel.Language language = adapter.getItem(sourceLangSelector.getSelectedItemPosition());
                if (isChecked) {
                    viewModel.downloadLanguage(language);
                } else {
                    viewModel.deleteLanguage(language);
                }
            }
        });
        targetSyncButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TranslateViewModel.Language language = adapter.getItem(targetLangSelector.getSelectedItemPosition());
                if (isChecked) {
                    viewModel.downloadLanguage(language);
                } else {
                    viewModel.deleteLanguage(language);
                }
            }
        });

        // Translate input text as it is typed
        srcTextView.addTextChangedListener(new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setProgressText(targetTextView);
                viewModel.sourceText.postValue(s.toString());
            }
        });
        viewModel.translatedText.observe(this, new Observer<TranslateViewModel.ResultOrError>() {
            @Override
            public void onChanged(TranslateViewModel.ResultOrError resultOrError) {
                if (resultOrError.error != null) {
                    srcTextView.setError(resultOrError.error.getLocalizedMessage());
                } else {
                    targetTextView.setText(resultOrError.result);
                }
            }
        });

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(this, new Observer<List<String>> () {
            @Override
            public void onChanged(@Nullable List<String> translateRemoteModels) {
                String output = getResources ().getString(R.string.downloaded_models_label,
                        translateRemoteModels);
                downloadedModelsTextView.setText(output);
                sourceSyncButton.setChecked(translateRemoteModels.contains(
                        adapter.getItem(sourceLangSelector.getSelectedItemPosition()).getCode()));
                targetSyncButton.setChecked(translateRemoteModels.contains(
                        adapter.getItem(targetLangSelector.getSelectedItemPosition()).getCode()));
            }
        });


        /**************************************************************************
         *
         *
         * translate voice
         *
         **************************************************************************/

        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
              startVoiceInput();


            }
        });





        /*********************************************************************
         *
         *
         * CAMERA
         *
         **********************************************************************/


        mCamBtn = (ImageButton) findViewById(R.id.camb);
        mCamBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestPermissions ();
                if (ActivityCompat.checkSelfPermission(getApplicationContext (), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    String filename = System.currentTimeMillis() + ".jpg";

                    ContentValues values = new ContentValues();
                    values.put( MediaStore.Images.Media.TITLE, filename);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else {
                    askCameraPermission();
                }

            }
        });

        /*********************************************
         *
         *GALLERY
         *************************************/


        mgall = (ImageButton) findViewById(R.id.camGal);
        mgall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });



        /////////////////////////////////////////
        srcTextView.setMovementMethod(new ScrollingMovementMethod ());

        /********************************
          mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

           mCamBtn = (ImageButton) findViewById(R.id.camb);
           mCamBtn.setOnClickListener(new View.OnClickListener() {

               @Override
               public void onClick(View v) {


                   if (ActivityCompat.checkSelfPermission(getApplicationContext (), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                       startTextRecognizer();
                   } else {
                       askCameraPermission();
                   }
               }
           });*/




    }
    private void askCameraPermission() {
        Log.i ( "rrrrrrr", "askperm" );

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale ( this,
                Manifest.permission.CAMERA )) {
            ActivityCompat.requestPermissions ( this, permissions, RC_HANDLE_CAMERA_PERM );
            return;
        }
    }
    private void setProgressText(TextView tv) {
        tv.setText(getResources ().getString(R.string.translate_progress));
    }


    private void startVoiceInput() {
        Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Radia is listening to you :D");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }



    /****************************************
     * cam2 Methodes
*********************/


    private void requestPermissions()
    {
        List<String> requiredPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.CAMERA);
        }

        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    requiredPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUESTS);
        }
    }



    private void inspectFromBitmap(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        try {
            if (!textRecognizer.isOperational()) {
                new AlertDialog.
                        Builder(this).
                        setMessage("Text recognizer could not be set up on your device").show();
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }
            Collections.sort(textBlocks, new Comparator<TextBlock> () {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedText = new StringBuilder();
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText.append(textBlock.getValue());
                    detectedText.append("\n");
                }
            }

            srcTextView.setText(detectedText);
        }
        finally {
            textRecognizer.release();
        }
    }

    private void inspect(Uri uri) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            inspectFromBitmap(bitmap);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed to find the file: " + uri, e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close InputStream", e);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    inspect(data.getData());
                }
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        inspect(imageUri);
                    }
                }
                break;

            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra ( RecognizerIntent.EXTRA_RESULTS );
                    srcTextView.setText ( result.get ( 0 ) );
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


}