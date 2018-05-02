package com.raccoondev85.plugin.kakao;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kakao.auth.ApiResponseCallback;
import com.kakao.kakaolink.internal.KakaoTalkLinkProtocol;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.message.template.*;
import com.kakao.network.callback.*;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.network.storage.ImageDeleteResponse;
import com.kakao.network.storage.ImageUploadResponse;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.MediaUtils;
import com.raccoondev85.plugin.kakao.KakaoResources;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class KakaoCordovaSDK extends CordovaPlugin {

    private static final String LOG_TAG = "KakaoCordovaSDK";
    private static volatile Activity currentActivity;
    private SessionCallback sesstionCallback;
    private KakaoMeResponseCallback kakaoMeResponseCallback;
    private KakaoLinkResponseCallback kakaoLinkResponseCallback;
    private KakaoLinkImageUploadResponseCallback kakaoLinkImageUploadResponseCallback;
    private KakaoLinkImageDeleteResponseCallback kakaoLinkImageDeleteResponseCallback;
    private static AuthType[] customAuthTypes;
    private static final int GALLERY_REQUEST_CODE = 9238;
    private String[] STORAGE_PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE };
    private final int REQUEST_EXTERNAL_STORAGE = 1;


    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.v(LOG_TAG, "kakao : initialize");
        super.initialize(cordova, webView);
        currentActivity = this.cordova.getActivity();
        try{
            KakaoSDK.init(new KakaoSDKAdapter());
            KakaoResources.initResources(cordova.getActivity().getApplication());
        }catch (Exception e){

        }


    }

    public boolean execute(final String action, JSONArray options, final CallbackContext callbackContext) throws JSONException {
        Log.v(LOG_TAG, "kakao : execute " + action);
        cordova.setActivityResultCallback(this);

        if (action.equals("login")) {
            sesstionCallback = new SessionCallback(callbackContext);
            Session.getCurrentSession().addCallback(sesstionCallback);
            this.login(callbackContext, options);
            return true;
        } else if (action.equals("logout")) {
            sesstionCallback = new SessionCallback(callbackContext);
            Session.getCurrentSession().addCallback(sesstionCallback);
            this.logout(callbackContext);
            return true;
	    } else if (action.equals("unlinkApp")) {
            sesstionCallback = new SessionCallback(callbackContext);
            Session.getCurrentSession().addCallback(sesstionCallback);
            this.unlinkApp(callbackContext);
            return true;
	    } else if (action.equals("getAccessToken")) {
            sesstionCallback = new SessionCallback(callbackContext);
            Session.getCurrentSession().addCallback(sesstionCallback);
            this.getAccessToken(callbackContext);
            return true;
        }  else if (action.equals("requestMe")) {
            sesstionCallback = new SessionCallback(callbackContext);
            Session.getCurrentSession().addCallback(sesstionCallback);
            this.requestMe(callbackContext);
            return true;
        } else if (action.equals("sendLinkFeed")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
            this.sendLinkFeed(callbackContext, options);
	        return true;
        } else if (action.equals("sendLinkList")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
	        this.sendLinkList(callbackContext, options);
	        return true;
        } else if (action.equals("sendLinkLocation")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
	        this.sendLinkLocation(callbackContext, options);
	        return true;
        } else if (action.equals("sendLinkCommerce")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
	        this.sendLinkCommerce(callbackContext, options);
	        return true;
        } else if (action.equals("sendLinkText")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
	        this.sendLinkText(callbackContext, options);
	        return true;
        } else if (action.equals("sendLinkScrap")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
	        this.sendLinkScrap(callbackContext, options);
	        return true;
        } else if (action.equals("sendLinkCustom")) {
            kakaoLinkResponseCallback = new KakaoLinkResponseCallback(callbackContext);
	        this.sendLinkCustom(callbackContext, options);
	        return true;
        } else if (action.equals("uploadImage")) {
            kakaoLinkImageUploadResponseCallback = new KakaoLinkImageUploadResponseCallback(callbackContext);
            this.uploadImage(callbackContext, options);
	        return true;
        } else if (action.equals("deleteUploadedImage")) {
            kakaoLinkImageDeleteResponseCallback = new KakaoLinkImageDeleteResponseCallback(callbackContext);
            this.deleteUploadedImage(callbackContext, options);
	        return true;
        } else if (action.equals("postStory")) {
	        this.postStory(callbackContext, options);
	        return true;
        }
        return false;
    }

    private void login(final CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject parameters = options.getJSONObject(0);
            if (parameters.has("authTypes")){
                JSONArray authTypes = new JSONArray(parameters.getString("authTypes"));
                setCustomAuthTypes(authTypes);
            }
        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        } finally {
            onClickLoginButton(getAuthTypes());
        }
    }

    private void requestMe(final CallbackContext callbackContext){
        kakaoMeResponseCallback = new KakaoMeResponseCallback(callbackContext);
        UserManagement.getInstance().requestMe(kakaoMeResponseCallback);
    }

    private void logout(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Log.v(LOG_TAG, "kakao : onCompleteLogout");
                        callbackContext.success();
                    }
                });
            }
        });
    }

    private void unlinkApp(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
                        Session.getCurrentSession().checkAndImplicitOpen();
                    }

                    @Override
                    public void onSuccess(Long userId) {
                        callbackContext.success(Long.toString(userId));
                    }

                    @Override
                    public void onNotSignedUp() {
                        KakaoCordovaErrorHandler.errorHandler(callbackContext, "this user is not signed up");
                    }
                });
            }
        });
    }

    private void getAccessToken(CallbackContext callbackContext) {
        String accessToken = Session.getCurrentSession().getTokenInfo().getAccessToken();
        callbackContext.success(accessToken);
    }

    private void sendLinkFeed(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "feed template is null.");
                return;
            }
            if (!object.has("content")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "content is null.");
                return;
            }

            ContentObject contentObject = getContentObject(object.getJSONObject("content"));
            if(contentObject == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "Either Content or Content.title/link/imageURL is null.");
                return;
            }

            FeedTemplate.Builder feedTemplateBuilder = new FeedTemplate.Builder(contentObject);

            if (object.has("social")){
                SocialObject socialObject = getSocialObject(object.getJSONObject("social"));
                if(socialObject != null){
                    feedTemplateBuilder.setSocial(socialObject);
                }
            }

            addButtonsArray(object, feedTemplateBuilder);

            KakaoLinkService.getInstance().sendDefault(currentActivity, feedTemplateBuilder.build(), kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void sendLinkList(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "list template is null.");
                return;
            }
            if (!object.has("headerTitle")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "headerTitle is null.");
                return;
            }
            if (!object.has("headerLink")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "headerLink is null.");
                return;
            }
            if (!object.has("contents")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "contents is null.");
                return;
            }

            LinkObject linkObject = getLinkObject(object.getJSONObject("headerLink"));
            if(linkObject == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "headerLink is null.");
                return;
            }
            ListTemplate.Builder listTemplateBuilder = ListTemplate.newBuilder(
                    object.getString("headerTitle"),linkObject);

            if(!addContentsArray(object, listTemplateBuilder)){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "Either Content or Content.title/link/imageURL is null.");
                return;
            }

            addButtonsArray(object, listTemplateBuilder);

            KakaoLinkService.getInstance().sendDefault(currentActivity, listTemplateBuilder.build(), kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void sendLinkLocation(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "location template is null.");
                return;
            }
            if (!object.has("content")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "content is null.");
                return;
            }
            if (!object.has("address")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "address is null.");
                return;
            }

            ContentObject contentObject = getContentObject(object.getJSONObject("content"));
            if(contentObject == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "Either Content or Content.title/link/imageURL is null.");
                return;
            }

            LocationTemplate.Builder locationTemplateBuilder = LocationTemplate.newBuilder(object.getString("address"), contentObject);

            if (object.has("addressTitle")){
                locationTemplateBuilder.setAddressTitle(object.getString("addressTitle"));
            }
            if (object.has("social")){
                SocialObject socialObject = getSocialObject(object.getJSONObject("social"));
                if(socialObject != null){
                    locationTemplateBuilder.setSocial(socialObject);
                }
            }

            addButtonsArray(object, locationTemplateBuilder);

            KakaoLinkService.getInstance().sendDefault(currentActivity, locationTemplateBuilder.build(), kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void sendLinkCommerce(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "commerce template is null.");
                return;
            }
            if (!object.has("content")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "content is null.");
                return;
            }
            if (!object.has("commerce")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "commerce is null.");
                return;
            }

            ContentObject contentObject = getContentObject(object.getJSONObject("content"));
            if(contentObject == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "Either Content or Content.title/link/imageURL is null.");
                return;
            }

            CommerceDetailObject commerceDetailObject = getCommerceDetailObject(object.getJSONObject("commerce"));
            if(commerceDetailObject == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "regularPrice is null.");
                return;
            }

            CommerceTemplate.Builder commerceTemplateBuilder = CommerceTemplate.newBuilder(contentObject, commerceDetailObject);

            addButtonsArray(object, commerceTemplateBuilder);

            KakaoLinkService.getInstance().sendDefault(currentActivity, commerceTemplateBuilder.build(), kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void sendLinkText(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "text template is null.");
                return;
            }
            if (!object.has("text")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "text is null.");
                return;
            }
            if (!object.has("link")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "link is null.");
                return;
            }

            LinkObject linkObject = getLinkObject(object.getJSONObject("link"));
            if(linkObject == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "link is null.");
                return;
            }
            TextTemplate.Builder textTemplateBuilder = TextTemplate.newBuilder(
                    object.getString("text"),linkObject);


            if (object.has("buttonTitle")){
                textTemplateBuilder.setButtonTitle(object.getString("buttonTitle"));
            }

            addButtonsArray(object, textTemplateBuilder);

            KakaoLinkService.getInstance().sendDefault(currentActivity, textTemplateBuilder.build(), kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void sendLinkScrap(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "scrap template is null.");
                return;
            }
            if (!object.has("url")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "url is null.");
                return;
            }

            KakaoLinkService.getInstance().sendScrap(currentActivity, object.getString("url"), kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void sendLinkCustom(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);
            if(object == null){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "custom template is null.");
                return;
            }
            if (!object.has("templateId")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "templateId is null.");
                return;
            }

            String templateId = object.getString("templateId");

            Map<String, String> templateArgs = new HashMap<String, String>();
            if (object.has("title")){
                templateArgs.put("title", object.getString("title"));
            }
            if (object.has("description")){
                templateArgs.put("description", object.getString("description"));
            }


            KakaoLinkService.getInstance().sendCustom(currentActivity, templateId, templateArgs, kakaoLinkResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void uploadImage(CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);

            if (!object.has("fileOrUrl")){
                uploadImageForLink();
            }

            if("file".equals(object.getString("fileOrUrl"))){
                uploadImageForLink();
            }else if("url".equals(object.getString("fileOrUrl"))){
                if(!object.has("url")){
                    KakaoCordovaErrorHandler.errorHandler(callbackContext, "url is null.");
                    return;
                }
                scrapRemoteImage(object.getString("url"));
            }else{
                uploadImageForLink();
            }

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void uploadImageForLink() {
        if(!cordova.hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            cordova.requestPermissions(this, REQUEST_EXTERNAL_STORAGE, STORAGE_PERMISSIONS);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        currentActivity.startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void uploadLocalImage(Uri uri) {


        try {
            File imageFile = new File(MediaUtils.getImageFilePathFromUri(uri, currentActivity));

            KakaoLinkService.getInstance().uploadImage(currentActivity, false, imageFile, kakaoLinkImageUploadResponseCallback);
        } catch (Exception e) {
            KakaoCordovaErrorHandler.errorHandler(kakaoLinkImageUploadResponseCallback.callbackContext, new ErrorResult(e));
        }
    }

    private void scrapRemoteImage(String url){

        try {
            KakaoLinkService.getInstance().scrapImage(currentActivity, false, url, kakaoLinkImageUploadResponseCallback);
        } catch (Exception e) {
            KakaoCordovaErrorHandler.errorHandler(kakaoLinkImageUploadResponseCallback.callbackContext, new ErrorResult(e));
        }
    }

    private void deleteUploadedImage(final CallbackContext callbackContext, JSONArray options) {
        try {
            final JSONObject object = options.getJSONObject(0);

            if (!object.has("url")){
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "KLDeleteImageConfig is null.");
                return;
            }

            String url = object.getString("url");

            KakaoLinkService.getInstance().deleteImageWithUrl(currentActivity, url, kakaoLinkImageDeleteResponseCallback);

        } catch(Exception e) {
            e.printStackTrace();
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }
    }

    private void postStory(CallbackContext callbackContext, JSONArray options) {
        try {

            final JSONObject object = options.getJSONObject(0);
            if (object == null) {
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "KLPostStoryConfig is null.");
                return;
            }

            if (!object.has("post") || !object.has("appver")) {
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "post or appver is null.");
                return;
            }

            // Recommended: Use application context for parameter.
            KakaoStoryLink storyLink = KakaoStoryLink.getLink(currentActivity);

            // check, intent is available.
            if (!storyLink.isAvailableIntent()) {
                KakaoCordovaErrorHandler.errorHandler(callbackContext, "KakaoStory not installed.");
                return;
            }

            String post = object.getString("post");
            String appver = object.getString("appver");
            String appid = currentActivity.getPackageName();
            if(object.has("appid")){
                appid = object.getString("appid");
            }
            String appname = currentActivity.getResources().getString(KakaoResources.app_name);
            if(object.has("appname")){
                appname = object.getString("appname");
            }

            Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
            if(object.has("urlinfo")){

                JSONObject urlinfo = object.getJSONObject("urlinfo");
                if(!urlinfo.has("title")){
                    KakaoCordovaErrorHandler.errorHandler(callbackContext, "title in urlinfo is null.");
                    return;
                }
                urlInfoAndroid.put("title", urlinfo.getString("title"));
                if(urlinfo.has("desc")){
                    urlInfoAndroid.put("desc", urlinfo.getString("desc"));
                }
                if(urlinfo.has("imageURLs")){
                    JSONArray imageurl = urlinfo.getJSONArray("imageURLs");
                    if(imageurl != null){
                        String[] arr=new String[imageurl.length()];
                        for(int i=0; i<arr.length; i++) {
                            arr[i]=imageurl.optString(i);
                        }
                        urlInfoAndroid.put("imageurl", arr);
                    }

                }
                if(urlinfo.has("type")){

                    switch (urlinfo.getInt("type")) {
                        case 2:
                            urlInfoAndroid.put("type", "video");
                            break;
                        case 3:
                            urlInfoAndroid.put("type", "music");
                            break;
                        case 4:
                            urlInfoAndroid.put("type", "book");
                            break;
                        case 5:
                            urlInfoAndroid.put("type", "article");
                            break;
                        case 6:
                            urlInfoAndroid.put("type", "profile");
                            break;
                        default:
                            urlInfoAndroid.put("type", "website");
                            break;
                    }

                }
            }

            storyLink.openKakaoLink(currentActivity, post, appid, appver, appname, "UTF-8", urlInfoAndroid);
            callbackContext.success("success!");

        }catch (Exception e){
            KakaoCordovaErrorHandler.errorHandler(callbackContext, new ErrorResult(e));
        }



    }

    private ContentObject getContentObject(JSONObject object) {
        if(object == null){
            return null;
        }
        ContentObject.Builder contentObjectBuilder;
        try{
            LinkObject linkObject = getLinkObject(object.getJSONObject("link"));
            if(!object.has("title") || linkObject == null || !object.has("imageURL")){
                return null;
            }
            contentObjectBuilder = new ContentObject.Builder(object.getString("title"),
                    object.getString("imageURL"), linkObject);

            if(object.has("desc")){
                contentObjectBuilder.setDescrption(object.getString("desc"));
            }
            if(object.has("imageWidth")){
                contentObjectBuilder.setImageWidth(object.getInt("imageWidth"));
            }
            if(object.has("imageHeight")){
                contentObjectBuilder.setImageHeight(object.getInt("imageHeight"));
            }
        }catch (Exception e){
            return null;
        }

        return contentObjectBuilder.build();
    }

    private CommerceDetailObject getCommerceDetailObject(JSONObject object) {
        if(object == null){
            return null;
        }
        CommerceDetailObject.Builder commerceDetailObjectBuilder;
        try {
            if(!object.has("regularPrice")){
                return null;
            }
            commerceDetailObjectBuilder = CommerceDetailObject.newBuilder(object.getInt("regularPrice"));
            if(object.has("discountPrice")){
                commerceDetailObjectBuilder.setDiscountPrice(object.getInt("discountPrice"));
            }
            if(object.has("discountRate")){
                commerceDetailObjectBuilder.setDiscountRate(object.getInt("discountRate"));
            }
            if(object.has("fixedDiscountPrice")){
                commerceDetailObjectBuilder.setFixedDiscountPrice(object.getInt("fixedDiscountPrice"));
            }

        }catch (Exception e){
            return null;
        }
        return commerceDetailObjectBuilder.build();
    }

    private SocialObject getSocialObject(JSONObject object) {
        if(object == null){
            return null;
        }
        SocialObject.Builder socialObjectBuilder = new SocialObject.Builder();
        try{
            if(object.has("likeCount")){
                socialObjectBuilder.setLikeCount(object.getInt("likeCount"));
            }
            if(object.has("commentCount")){
                socialObjectBuilder.setCommentCount(object.getInt("commentCount"));
            }
            if(object.has("sharedCount")){
                socialObjectBuilder.setSharedCount(object.getInt("sharedCount"));
            }
            if(object.has("viewCount")){
                socialObjectBuilder.setViewCount(object.getInt("viewCount"));
            }
            if(object.has("subscriberCount")){
                socialObjectBuilder.setSubscriberCount(object.getInt("subscriberCount"));
            }
        }catch (Exception e){
            return null;
        }
        return socialObjectBuilder.build();
    }

    private ButtonObject getButtonObject(JSONObject object) {
        if(object == null){
            return null;
        }
        ButtonObject buttonObject;
        try{
            LinkObject linkObject = getLinkObject(object.getJSONObject("link"));
            if(!object.has("title") || linkObject == null){
                return null;
            }
            buttonObject = new ButtonObject(object.getString("title"), linkObject);
        }catch (Exception e){
            return null;
        }

        return buttonObject;
    }
    private LinkObject getLinkObject(JSONObject object) {
        if(object == null){
            return null;
        }
        LinkObject.Builder linkObjectBuilder = new LinkObject.Builder();
        try{
            if(object.has("webURL")){
                linkObjectBuilder.setWebUrl(object.getString("webURL"));
            }
            if(object.has("mobileWebURL")){
                linkObjectBuilder.setMobileWebUrl(object.getString("mobileWebURL"));
            }
            if(object.has("androidExecutionParams")){
                linkObjectBuilder.setAndroidExecutionParams(object.getString("androidExecutionParams"));
            }
            if(object.has("iosExecutionParams")){
                linkObjectBuilder.setIosExecutionParams(object.getString("iosExecutionParams"));
            }
        }catch (Exception e){
            return null;
        }
        return linkObjectBuilder.build();
    }

    private void addButtonsArray(JSONObject object, Object template) {
        if(object == null){
            return;
        }
        try{
            if (object.has("buttons")){
                JSONArray buttons = new JSONArray(object.getString("buttons"));
                if(buttons.length() < 1){
                    return;
                }
                for(int i=0; i<buttons.length(); i++){
                    ButtonObject buttonObject = getButtonObject(buttons.getJSONObject(i));
                    if(buttonObject == null){
                        continue;
                    }
                    if(template instanceof FeedTemplate.Builder){
                        ((FeedTemplate.Builder) template).addButton(buttonObject);
                    }else if(template instanceof ListTemplate.Builder){
                        ((ListTemplate.Builder) template).addButton(buttonObject);
                    }else if(template instanceof LocationTemplate.Builder){
                        ((LocationTemplate.Builder) template).addButton(buttonObject);
                    }else if(template instanceof CommerceTemplate.Builder){
                        ((CommerceTemplate.Builder) template).addButton(buttonObject);
                    }else if(template instanceof TextTemplate.Builder){
                        ((TextTemplate.Builder) template).addButton(buttonObject);
                    }
                }
            }
        }catch (Exception e){
            return;
        }
    }

    private boolean addContentsArray(JSONObject object, ListTemplate.Builder template) {
        if(object == null){
            return false;
        }
        try{
            if (object.has("contents")){
                JSONArray contents = new JSONArray(object.getString("contents"));
                if(contents.length() < 1){
                    return false;
                }
                for(int i=0; i<contents.length(); i++){
                    ContentObject contentObject = getContentObject(contents.getJSONObject(i));
                    if(contentObject == null){
                        return false;
                    }
                    template.addContent(contentObject);
                }
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(LOG_TAG, "kakao : onActivityResult : " + requestCode + ", code: " + resultCode);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = intent.getData();
            uploadLocalImage(uri);
        }else if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, intent)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (kakaoLinkImageUploadResponseCallback == null) {
            return;
        }
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadImageForLink();
                } else {
                    KakaoCordovaErrorHandler.errorHandler(kakaoLinkImageUploadResponseCallback.callbackContext, "User did not agree to give storage permission.");
                }
                break;
            default:
                break;
        }

    }

    private JSONObject handleLoginResult(UserProfile userProfile, String accessToken) {
        Log.v(LOG_TAG, "kakao : handleLoginResult");
        JSONObject response = new JSONObject();
        try {
            Log.v(LOG_TAG, "kakao response: " + response);
            JSONObject userinfo = new JSONObject();

            userinfo.put("accessToken", accessToken);
            userinfo.put("id", userProfile.getId());
            userinfo.put("email", userProfile.getEmail());
            userinfo.put("emailVerified", userProfile.getEmailVerified());
            userinfo.put("nickname", userProfile.getNickname());
            userinfo.put("profileImagePath", userProfile.getProfileImagePath());
            userinfo.put("thumbnailImagePath", userProfile.getThumbnailImagePath());

            JSONObject prop = new JSONObject(userProfile.getProperties());
            JSONObject[] objs = new JSONObject[] { userinfo, prop };
            for (JSONObject obj : objs) {
                Iterator it = obj.keys();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    response.put(key, obj.get(key));
                }
            }
        } catch (JSONException e) {
            Log.v(LOG_TAG, "kakao : handleResult error - " + e.toString());
        }
        return response;
    }

    private JSONObject handleKakaoLinkImageUploadResponseResult(ImageUploadResponse imageUploadResponse) {
        Log.v(LOG_TAG, "kakao : handleKakaoLinkImageUploadResponseResult");
        JSONObject response = new JSONObject();
        try {
            Log.v(LOG_TAG, "kakao response: " + response);
            response.put("url", imageUploadResponse.getOriginal().getUrl());
            response.put("content_type", imageUploadResponse.getOriginal().getContentType());
            response.put("length", imageUploadResponse.getOriginal().getLength());
            response.put("height", imageUploadResponse.getOriginal().getHeight());
            response.put("width", imageUploadResponse.getOriginal().getWidth());


        } catch (JSONException e) {
            Log.v(LOG_TAG, "kakao : handleResult error - " + e.toString());
        }
        return response;
    }


    private JSONObject handleKakaoLinkResponseResult(KakaoLinkResponse kakaoLinkResponse) {
        Log.v(LOG_TAG, "kakao : handleKakaoLinkResponseResult");
        JSONObject response = new JSONObject();
        try {
            Log.v(LOG_TAG, "kakao response: " + response);
            response.put(KakaoTalkLinkProtocol.TEMPLATE_ID, kakaoLinkResponse.getTemplateId());
            response.put(KakaoTalkLinkProtocol.TEMPLATE_ARGS, kakaoLinkResponse.getTemplateArgs());
            response.put(KakaoTalkLinkProtocol.TEMPLATE_MSG, kakaoLinkResponse.getTemplateMsg());
            response.put(KakaoTalkLinkProtocol.WARNING_MSG, kakaoLinkResponse.getWarningMsg());
            response.put(KakaoTalkLinkProtocol.ARGUMENT_MSG, kakaoLinkResponse.getArgumentMsg());


        } catch (JSONException e) {
            Log.v(LOG_TAG, "kakao : handleResult error - " + e.toString());
        }
        return response;
    }

    private class KakaoLinkImageUploadResponseCallback extends ResponseCallback<ImageUploadResponse> {

        private CallbackContext callbackContext;

        public KakaoLinkImageUploadResponseCallback(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }


        @Override
        public void onFailure(ErrorResult errorResult) {
            KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
        }

        @Override
        public void onSuccess(ImageUploadResponse result) {
            callbackContext.success(result.getOriginal().getUrl());
//            callbackContext.success(handleKakaoLinkImageUploadResponseResult(result).toString());
        }
    }

    private class KakaoLinkImageDeleteResponseCallback extends ResponseCallback<ImageDeleteResponse> {

        private CallbackContext callbackContext;

        public KakaoLinkImageDeleteResponseCallback(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }


        @Override
        public void onFailure(ErrorResult errorResult) {
            KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
        }

        @Override
        public void onSuccess(ImageDeleteResponse result) {

            callbackContext.success("success " + result.toString());
        }
    }

    private class KakaoLinkResponseCallback extends ResponseCallback<KakaoLinkResponse> {

        private CallbackContext callbackContext;

        public KakaoLinkResponseCallback(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }


        @Override
        public void onFailure(ErrorResult errorResult) {
            KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
        }

        @Override
        public void onSuccess(KakaoLinkResponse result) {

            callbackContext.success("" + handleKakaoLinkResponseResult(result));
        }
    }

    private class KakaoMeResponseCallback extends MeResponseCallback {

        private CallbackContext callbackContext;

        public KakaoMeResponseCallback(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            KakaoCordovaErrorHandler.errorHandler(callbackContext, errorResult);
            Session.getCurrentSession().checkAndImplicitOpen();
        }

        @Override
        public void onSuccess(UserProfile userProfile) {
            callbackContext.success(handleLoginResult(userProfile, Session.getCurrentSession().getTokenInfo().getAccessToken()));
        }

        @Override
        public void onNotSignedUp() {
            KakaoCordovaErrorHandler.errorHandler(callbackContext, "this user is not signed up");
        }

    }

    private class SessionCallback implements ISessionCallback {

        private CallbackContext callbackContext;

        public SessionCallback(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }

        @Override
        public void onSessionOpened() {
            requestMe(callbackContext);
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) {
                KakaoCordovaErrorHandler.errorHandler(callbackContext, exception.toString());
            }
        }
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        currentActivity = currentActivity;
    }

    public static AuthType[] getCustomAuthTypes() {
        return customAuthTypes;
    }

    public static void setCustomAuthTypes(JSONArray _customAuthTypes) {
        if(_customAuthTypes == null){
            return;
        }
        try {
            Map<AuthType, MyAuthType> tempAuthTypes = new HashMap<AuthType, MyAuthType>();
            for(int i=0; i<_customAuthTypes.length(); i++){
//                Log.d(LOG_TAG, _customAuthTypes.get(i)+"");
                if(MyAuthType.AuthTypeTalk.getNumber() == Integer.parseInt(_customAuthTypes.get(i).toString())){
                    tempAuthTypes.put(AuthType.KAKAO_TALK, MyAuthType.AuthTypeTalk);
                } else if(MyAuthType.AuthTypeStory.getNumber() == Integer.parseInt(_customAuthTypes.get(i).toString())){
                    tempAuthTypes.put(AuthType.KAKAO_STORY, MyAuthType.AuthTypeStory);
                } else if(MyAuthType.AuthTypeAccout.getNumber() == Integer.parseInt(_customAuthTypes.get(i).toString())){
                    tempAuthTypes.put(AuthType.KAKAO_ACCOUNT, MyAuthType.AuthTypeAccout);
                }
            }
            AuthType[] types;
            if(tempAuthTypes.size() == 3){
                types = new AuthType[1];
                types[0] = AuthType.KAKAO_LOGIN_ALL;
            }else{
                types = new AuthType[tempAuthTypes.size()];
                int count = 0;
                for( AuthType key : tempAuthTypes.keySet() ){
                    types[count] = key;
                    count++;
                }

            }
//            Log.d(LOG_TAG, types.toString());
            customAuthTypes = types;
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    /**
     * Class KakaoSDKAdapter
     */
    private static class KakaoSDKAdapter extends KakaoAdapter {

        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return KakaoCordovaSDK.getCurrentActivity().getApplicationContext();
                }
            };
        }
    }


    private List<AuthType> getAuthTypes() {
        final List<AuthType> availableAuthTypes = new ArrayList();

        if (Session.getCurrentSession().getAuthCodeManager().isTalkLoginAvailable()) {
            availableAuthTypes.add(AuthType.KAKAO_TALK);
        }
        if (Session.getCurrentSession().getAuthCodeManager().isStoryLoginAvailable()) {
            availableAuthTypes.add(AuthType.KAKAO_STORY);
        }
        availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

        AuthType[] authTypes;

        if(getCustomAuthTypes() == null){
            authTypes = KakaoSDK.getAdapter().getSessionConfig().getAuthTypes();
        } else {
            authTypes = getCustomAuthTypes();
        }

        if (authTypes == null || authTypes.length == 0 || (authTypes.length == 1 && authTypes[0] == AuthType.KAKAO_LOGIN_ALL)) {
            authTypes = AuthType.values();
        }
        availableAuthTypes.retainAll(Arrays.asList(authTypes));

        // 개발자가 설정한 것과 available 한 타입이 없다면 직접계정 입력이 뜨도록 한다.
        if(availableAuthTypes.size() == 0){
            availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);
        }
        return availableAuthTypes;
    }

    private void onClickLoginButton(final List<AuthType> authTypes){
       if (authTypes.size() == 1) {
           openSession(authTypes.get(0));

       } else {
            final Item[] authItems = createAuthItemArray(authTypes);
            ListAdapter adapter = createLoginAdapter(authItems);
            final Dialog dialog = createLoginDialog(authItems, adapter);
            dialog.show();
       }
    }

    /**
     * 가능한 AuhType들이 담겨 있는 리스트를 인자로 받아 로그인 어댑터의 data source로 사용될 Item array를 반환한다.
     * @param authTypes 가능한 AuthType들을 담고 있는 리스트
     * @return 실제로 로그인 방법 리스트에 사용될 Item array
     */
    private Item[] createAuthItemArray(final List<AuthType> authTypes) {
        final List<Item> itemList = new ArrayList<Item>();


        if(authTypes.contains(AuthType.KAKAO_TALK)) {
            itemList.add(new Item(KakaoResources.com_kakao_kakaotalk_account, KakaoResources.talk, KakaoResources.com_kakao_kakaotalk_account_tts, AuthType.KAKAO_TALK));
        }
        if(authTypes.contains(AuthType.KAKAO_STORY)) {
            itemList.add(new Item(KakaoResources.com_kakao_kakaostory_account, KakaoResources.story, KakaoResources.com_kakao_kakaostory_account_tts, AuthType.KAKAO_STORY));
        }
        if(authTypes.contains(AuthType.KAKAO_ACCOUNT)){
            itemList.add(new Item(KakaoResources.com_kakao_other_kakaoaccount, KakaoResources.account, KakaoResources.com_kakao_other_kakaoaccount_tts, AuthType.KAKAO_ACCOUNT));
        }

        return itemList.toArray(new Item[itemList.size()]);
    }


    @SuppressWarnings("deprecation")
    private ListAdapter createLoginAdapter(final Item[] authItems) {
        /*
          가능한 auth type들을 유저에게 보여주기 위한 준비.
         */
        return new ArrayAdapter<Item>(
                cordova.getActivity(),
                android.R.layout.select_dialog_item,
                android.R.id.text1, authItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(KakaoResources.layout_login_item, parent, false);
                }

                ImageView imageView = convertView.findViewById(KakaoResources.login_method_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(cordova.getActivity().getResources().getDrawable(authItems[position].icon, getContext().getTheme()));
                } else {
                    imageView.setImageDrawable(cordova.getActivity().getResources().getDrawable(authItems[position].icon));
                }
                TextView textView = convertView.findViewById(KakaoResources.login_method_text);
                textView.setText(authItems[position].textId);
                return convertView;
            }
        };
    }

    /**
     * 실제로 유저에게 보여질 dialog 객체를 생성한다.
     * @param authItems 가능한 AuthType들의 정보를 담고 있는 Item array
     * @param adapter Dialog의 list view에 쓰일 adapter
     * @return 로그인 방법들을 팝업으로 보여줄 dialog
     */
    private Dialog createLoginDialog(final Item[] authItems, final ListAdapter adapter) {

        final Dialog dialog = new Dialog(cordova.getActivity(), KakaoResources.LoginDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(KakaoResources.layout_login_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        ListView listView = dialog.findViewById(KakaoResources.login_list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AuthType authType = authItems[position].authType;
                if (authType != null) {
                    openSession(authType);
                }
                dialog.dismiss();
            }
        });

        Button closeButton = dialog.findViewById(KakaoResources.login_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public void openSession(final AuthType authType) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Session.getCurrentSession().open(authType, cordova.getActivity());
            }
        });

    }



    /**
     * 각 로그인 방법들의 text, icon, 실제 AuthTYpe들을 담고 있는 container class.
     */
    private static class Item {
        final int textId;
        public final int icon;
        final int contentDescId;
        final AuthType authType;
        Item(final int textId, final Integer icon, final int contentDescId, final AuthType authType) {
            this.textId = textId;
            this.icon = icon;
            this.contentDescId = contentDescId;
            this.authType = authType;
        }
    }

    private enum MyAuthType {
        AuthTypeTalk(1),
        AuthTypeStory(2),
        AuthTypeAccout(3),
        AuthTypeAll(4);
        private final int number;

        MyAuthType(int i) {
            this.number = i;
        }

        public int getNumber() {
            return number;
        }

        public static MyAuthType valueOf(int number){
            if(number == AuthTypeTalk.getNumber()) {
                return AuthTypeTalk;
            } else if (number == AuthTypeStory.getNumber()) {
                return AuthTypeStory;
            } else if (number == AuthTypeAccout.getNumber()) {
                return AuthTypeAccout;
            }  else if (number == AuthTypeAll.getNumber()) {
                return AuthTypeAll;
            } else {
                return null;
            }
        }
    }
}
