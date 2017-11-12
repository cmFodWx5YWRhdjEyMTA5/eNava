

package  com.enavamaratha.enavamaratha.parser;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.DropBoxManager;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import  com.enavamaratha.enavamaratha.Constants;
import  com.enavamaratha.enavamaratha.MainApplication;
import com.enavamaratha.enavamaratha.provider.DatabaseHelper;
import  com.enavamaratha.enavamaratha.provider.FeedData;
import  com.enavamaratha.enavamaratha.provider.FeedData.EntryColumns;
import  com.enavamaratha.enavamaratha.provider.FeedData.FeedColumns;
import  com.enavamaratha.enavamaratha.provider.FeedData.FilterColumns;
import  com.enavamaratha.enavamaratha.service.FetcherService;
import com.enavamaratha.enavamaratha.utils.DeleteFeeds;
import  com.enavamaratha.enavamaratha.utils.HtmlUtils;
import  com.enavamaratha.enavamaratha.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.EntityUtils;

public class RssAtomParser extends DefaultHandler {
    private static final String TAG = RssAtomParser.class.getSimpleName();

    private static final String AND_SHARP = "&#";
    private static final String HTML_TEXT = "text/html";
    private static final String HTML_TAG_REGEX = "<(.|\n)*?>";

    private static final String TAG_RSS = "rss";
    private static final String TAG_RDF = "rdf";
    private static final String TAG_FEED = "feed";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_ITEM = "item";
    private static final String TAG_UPDATED = "updated";
    private static final String TAG_TITLE = "title";
    private static final String TAG_LINK = "link";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_MEDIA_DESCRIPTION = "media:description";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_MEDIA_CONTENT = "media:content";
    private static final String TAG_ENCODED_CONTENT = "encoded";
    private static final String TAG_SUMMARY = "summary";
    private static final String TAG_PUBDATE = "pubDate";
    private static final String TAG_PUBLISHED = "published";
    private static final String TAG_DATE = "date";
    private static final String TAG_LAST_BUILD_DATE = "lastBuildDate";
    private static final String TAG_ENCLOSURE = "enclosure";
    private static final String TAG_GUID = "guid";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_CREATOR = "creator";
    private static final String TAG_NAME = "name";

    private static final String ATTRIBUTE_URL = "url";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_LENGTH = "length";
    private static final String ATTRIBUTE_REL = "rel";

    //private static final String[][] TIMEZONES_REPLACE = {{"GMT", "+0000"},{"UTC" , "+0530"}};

    private static final String TAG_ACTIVITY="RssAtomParser";

    private static final String[][] TIMEZONES_REPLACE = {{"MEST", "+0200"}, {"EST", "-0500"}, {"PST", "-0800"}};

    private static final DateFormat[] PUBDATE_DATE_FORMATS = {
            new SimpleDateFormat("d' 'MMM' 0'yy' 'HH:mm:ss' 'Z", Locale.US),
            new SimpleDateFormat("d' 'MMM' 'yyyy' 'HH:mm:ss' 'ZZZZ", Locale.US),
            new SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss' 'Z", Locale.US),
            new SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss' 'z", Locale.US),
            new SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss", Locale.US)
    };

    private static final DateFormat[] UPDATE_DATE_FORMATS = {
            new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSz", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd", Locale.US)
    };

    private final Date mRealLastUpdateDate;
    private final String mId;
    private final Uri mFeedEntriesUri;
    private final String mFeedName;
    private final String mFeedBaseUrl;
    private final Date mKeepDateBorder;
    private final FeedFilters mFilters;
    private final ArrayList<ContentProviderOperation> mInserts = new ArrayList<>();
    private final ArrayList<ArrayList<String>> mInsertedEntriesImages = new ArrayList<>();
    private long mNewRealLastUpdate;
    private boolean mEntryTagEntered = false;
    private boolean mTitleTagEntered = false;
    private boolean mUpdatedTagEntered = false;
    private boolean mLinkTagEntered = false;
    private boolean mDescriptionTagEntered = false;
    private boolean mPubDateTagEntered = false;
    private boolean mPublishedTagEntered = false;
    private boolean mDateTagEntered = false;
    private boolean mLastBuildDateTagEntered = false;
    private boolean mGuidTagEntered = false;
    private boolean mAuthorTagEntered = false;
    private StringBuilder mTitle;
    private StringBuilder mDateStringBuilder;
    private String mFeedLink;
    private Date mEntryDate;
    private Date mEntryUpdateDate;
    private Date mPreviousEntryDate;
    private Date mPreviousEntryUpdateDate;
    private StringBuilder mEntryLink;
    private StringBuilder mDescription;
    private StringBuilder mEnclosure;
    private int mNewCount = 0;
    private String mFeedTitle;
    private boolean mDone = false;
    private boolean mFetchImages = false;
    private boolean mRetrieveFullText = false;
    private boolean mCancelled = false;
    private long mNow = System.currentTimeMillis();
    private StringBuilder mGuid;
    private StringBuilder mAuthor, mTmpAuthor;
    final String DATABASE_NAME = "FeedEx.db";
    SQLiteDatabase db;

    // Sharedpref file name
    private static final String PREF_NAME = "MyPrefFileShred";
    // User name (make variable public to access from outside)
    public static final String KEY_TODAY_DATE_ANDTIME = "TimeToday";
    // Shared Preferences
    SharedPreferences settingsShrepref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    String  TodayDate ;
    private String guidString;



    public RssAtomParser(Date realLastUpdateDate, long keepDateBorderTime, final String id, String feedName, String url, boolean retrieveFullText) {
        mKeepDateBorder = new Date(keepDateBorderTime);
        mRealLastUpdateDate = realLastUpdateDate;
        mNewRealLastUpdate = realLastUpdateDate.getTime();
        mId = id;
        mFeedName = feedName;
        mFeedEntriesUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(id);
        mRetrieveFullText = retrieveFullText;
        mFilters = new FeedFilters(id);
        mFeedBaseUrl = NetworkUtils.getBaseUrl(url);
    }

    private static String unescapeTitle(String title) {
        String result = title.replace(Constants.AMP_SG, Constants.AMP).replaceAll(HTML_TAG_REGEX, "").replace(Constants.HTML_LT, Constants.LT)
                .replace(Constants.HTML_GT, Constants.GT).replace(Constants.HTML_QUOT, Constants.QUOT)
                .replace(Constants.HTML_APOSTROPHE, Constants.APOSTROPHE);

        if (result.contains(AND_SHARP)) {
            return Html.fromHtml(result, null, null).toString();
        } else {
            return result;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (TAG_UPDATED.equals(localName)) {
            mUpdatedTagEntered = true;
            mDateStringBuilder = new StringBuilder();
        } else if (TAG_ENTRY.equals(localName) || TAG_ITEM.equals(localName)) {
            mEntryTagEntered = true;
            mDescription = null;
            mEntryLink = null;

            // Save the previous (if no date are found for this entry)
            mPreviousEntryDate = mEntryDate;
            mPreviousEntryUpdateDate = mEntryUpdateDate;
            mEntryDate = null;
            mEntryUpdateDate = null;

            // This is the retrieved feed title
            if (mFeedTitle == null && mTitle != null && mTitle.length() > 0) {
                mFeedTitle = mTitle.toString();
            }
            mTitle = null;
        } else if (TAG_TITLE.equals(localName)) {
            if (mTitle == null) {
                mTitleTagEntered = true;
                mTitle = new StringBuilder();
            }
        } else if (TAG_LINK.equals(localName)) {
            if (mAuthorTagEntered) {
                return;
            }
            if (TAG_ENCLOSURE.equals(attributes.getValue("", ATTRIBUTE_REL))) {
                startEnclosure(attributes, attributes.getValue("", ATTRIBUTE_HREF));
            } else {
                // Get the link only if we don't have one or if its the good one (html)
                if (mEntryLink == null || HTML_TEXT.equals(attributes.getValue("", ATTRIBUTE_TYPE))) {
                    mEntryLink = new StringBuilder();

                    boolean foundLink = false;
                    String href = attributes.getValue("", ATTRIBUTE_HREF);
                    if (!TextUtils.isEmpty(href)) {
                        mEntryLink.append(href);
                        foundLink = true;
                        mLinkTagEntered = false;
                    } else {
                        mLinkTagEntered = true;
                    }

                    if (!foundLink) {
                        mLinkTagEntered = true;
                    }
                }
            }
        } else if ((TAG_DESCRIPTION.equals(localName) && !TAG_MEDIA_DESCRIPTION.equals(qName))
                || (TAG_CONTENT.equals(localName) && !TAG_MEDIA_CONTENT.equals(qName))) {
            mDescriptionTagEntered = true;
            mDescription = new StringBuilder();
        } else if (TAG_SUMMARY.equals(localName)) {
            if (mDescription == null) {
                mDescriptionTagEntered = true;
                mDescription = new StringBuilder();
            }
        } else if (TAG_PUBDATE.equals(localName)) {
            mPubDateTagEntered = true;
            mDateStringBuilder = new StringBuilder();
        } else if (TAG_PUBLISHED.equals(localName)) {
            mPublishedTagEntered = true;
            mDateStringBuilder = new StringBuilder();
        } else if (TAG_DATE.equals(localName)) {
            mDateTagEntered = true;
            mDateStringBuilder = new StringBuilder();
        } else if (TAG_LAST_BUILD_DATE.equals(localName)) {
            mLastBuildDateTagEntered = true;
            mDateStringBuilder = new StringBuilder();
        } else if (TAG_ENCODED_CONTENT.equals(localName)) {
            mDescriptionTagEntered = true;
            mDescription = new StringBuilder();
        } else if (TAG_ENCLOSURE.equals(localName)) {
            startEnclosure(attributes, attributes.getValue("", ATTRIBUTE_URL));
        } else if (TAG_GUID.equals(localName)) {
            mGuidTagEntered = true;
            mGuid = new StringBuilder();
        } else if (TAG_NAME.equals(localName) || TAG_AUTHOR.equals(localName) || TAG_CREATOR.equals(localName)) {
            mAuthorTagEntered = true;
            if (mTmpAuthor == null) {
                mTmpAuthor = new StringBuilder();
            }
        }
    }

    private void startEnclosure(Attributes attributes, String url) {
        if (mEnclosure == null && url != null) { // fetch the first enclosure only
            mEnclosure = new StringBuilder(url);
            mEnclosure.append(Constants.ENCLOSURE_SEPARATOR);

            String value = attributes.getValue("", ATTRIBUTE_TYPE);

            if (value != null) {
                mEnclosure.append(value);
            }
            mEnclosure.append(Constants.ENCLOSURE_SEPARATOR);
            value = attributes.getValue("", ATTRIBUTE_LENGTH);
            if (value != null) {
                mEnclosure.append(value);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (mTitleTagEntered) {
            mTitle.append(ch, start, length);
        } else if (mLinkTagEntered) {
            mEntryLink.append(ch, start, length);
        } else if (mDescriptionTagEntered) {
            mDescription.append(ch, start, length);
        } else if (mUpdatedTagEntered || mPubDateTagEntered || mPublishedTagEntered || mDateTagEntered || mLastBuildDateTagEntered) {
            mDateStringBuilder.append(ch, start, length);
        } else if (mGuidTagEntered) {
            mGuid.append(ch, start, length);
        } else if (mAuthorTagEntered) {
            mTmpAuthor.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (TAG_TITLE.equals(localName))
        {
            mTitleTagEntered = false;
        }
        else if ((TAG_DESCRIPTION.equals(localName) && !TAG_MEDIA_DESCRIPTION.equals(qName)) || TAG_SUMMARY.equals(localName)
                || (TAG_CONTENT.equals(localName) && !TAG_MEDIA_CONTENT.equals(qName)) || TAG_ENCODED_CONTENT.equals(localName))
        {
            mDescriptionTagEntered = false;
        } else if (TAG_LINK.equals(localName))
        {
            mLinkTagEntered = false;

            if (mFeedLink == null && !mEntryTagEntered && TAG_LINK.equals(qName))
            { // Skip <atom10:link> tags
                mFeedLink = mEntryLink.toString();
            }
        } else if (TAG_UPDATED.equals(localName))
        {
            mEntryUpdateDate = parseUpdateDate(mDateStringBuilder.toString());
            mUpdatedTagEntered = false;
        } else if (TAG_PUBDATE.equals(localName))
        {
            mEntryDate = parsePubdateDate(mDateStringBuilder.toString());
            //Log.i(TAG,"Parse Pub Date Of Post :"+mEntryDate);
            mPubDateTagEntered = false;
        } else if (TAG_PUBLISHED.equals(localName))
        {
            mEntryDate = parsePubdateDate(mDateStringBuilder.toString());
            mPublishedTagEntered = false;
        } else if (TAG_LAST_BUILD_DATE.equals(localName))
        {
            mEntryDate = parsePubdateDate(mDateStringBuilder.toString());
          //  Log.i(TAG,"Parse Last Build Date of Post  :"+mEntryDate);
            mLastBuildDateTagEntered = false;
        }
        else if (TAG_DATE.equals(localName))
        {
            mEntryDate = parseUpdateDate(mDateStringBuilder.toString());
            mDateTagEntered = false;
        }
        else if (TAG_ENTRY.equals(localName) || TAG_ITEM.equals(localName))
        {
            mEntryTagEntered = false;

            boolean updateOnly = false;
            // Old mEntryDate but recent update date => we need to not insert it!
            if (mEntryUpdateDate != null && mEntryDate != null && (mEntryDate.before(mRealLastUpdateDate) || mEntryDate.before(mKeepDateBorder)))
            {
                updateOnly = true;
                if (mEntryUpdateDate.after(mEntryDate))
                {
                    mEntryDate = mEntryUpdateDate;
                }
            }
            else if (mEntryDate == null && mEntryUpdateDate != null)
            { // only one updateDate, copy it into mEntryDate
                mEntryDate = mEntryUpdateDate;
            }
            else if (mEntryDate == null && mEntryUpdateDate == null)
            { // nothing, we need to retrieve the previous date
                mEntryDate = mPreviousEntryDate;
                mEntryUpdateDate = mPreviousEntryUpdateDate;
            }

           //    Log.i(TAG,"Calling GetPostd Method ");




            // run only once in a day
           /* DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = new Date();
            TodayDate   = dateFormat.format(date);

            settingsShrepref = MainApplication.getContext().getSharedPreferences(PREF_NAME, 0);
            editor = settingsShrepref.edit();

            String Val=settingsShrepref.getString(KEY_TODAY_DATE_ANDTIME,"");

            Log.i(TAG,"Stored Val : "+Val);
            Log.i(TAG,"Today date :"+ TodayDate);
            // check date is not equal todays date
            if(!Val.equals(TodayDate))
            {

                editor.putString(KEY_TODAY_DATE_ANDTIME, TodayDate);
                // commit changes
                editor.commit();

                String Value = settingsShrepref.getString(KEY_TODAY_DATE_ANDTIME, null);
                Log.d(TAG,"after Insertion data Date"+ Value);
            }*/



              //



            if (mTitle != null &&((mEntryDate == null) || (mEntryDate.after(mRealLastUpdateDate) && mEntryDate.after(mKeepDateBorder))))
            {
                ContentValues values = new ContentValues();
  //              dbb = new DatabaseHelper(new Handler(), MainApplication.getContext());
//                database = dbb.getWritableDatabase();

                Log.i(TAG,"Getting Updated Content");
                if (mEntryDate != null && mEntryDate.getTime() > mNewRealLastUpdate)
                {
                    mNewRealLastUpdate = mEntryDate.getTime();
                }



                String improvedTitle = unescapeTitle(mTitle.toString().trim());
                values.put(EntryColumns.TITLE, improvedTitle);
                String improvedContent = null;
                String mainImageUrl = null;
                ArrayList<String> imagesUrls = null;
                ArrayList<String> PostGuid = null;




                if (mDescription != null)
                {
                    // Improve the description
                    improvedContent = HtmlUtils.improveHtmlContent(mDescription.toString(), mFeedBaseUrl);
                    if (mFetchImages)
                    {
                        imagesUrls = HtmlUtils.getImageURLs(improvedContent);
                        if (!imagesUrls.isEmpty())
                        {
                            mainImageUrl = HtmlUtils.getMainImageURL(imagesUrls);
                        }
                    } else
                    {
                        mainImageUrl = HtmlUtils.getMainImageURL(improvedContent);
                    }

                    if (improvedContent != null)
                    {
                        values.put(EntryColumns.ABSTRACT, improvedContent);
                    }
                }

                if (mainImageUrl != null)
                {
                    values.put(EntryColumns.IMAGE_URL, mainImageUrl);
                }

                // Try to find if the entry is not filtered and need to be processed
                if (!mFilters.isEntryFiltered(improvedTitle, improvedContent)) {

                    if (mAuthor != null) {
                        values.put(EntryColumns.AUTHOR, mAuthor.toString());
                    }

                    String enclosureString = null;
                    StringBuilder existenceStringBuilder = new StringBuilder(EntryColumns.LINK).append(Constants.DB_ARG);

                    if (mEnclosure != null && mEnclosure.length() > 0)
                    {
                        enclosureString = mEnclosure.toString();
                        values.put(EntryColumns.ENCLOSURE, enclosureString);
                        existenceStringBuilder.append(Constants.DB_AND).append(EntryColumns.ENCLOSURE).append(Constants.DB_ARG);
                    }

                     guidString = null;

                    if (mGuid != null && mGuid.length() > 0)
                    {
                        guidString = mGuid.toString();
                        values.put(EntryColumns.GUID, guidString);
                        existenceStringBuilder.append(Constants.DB_AND).append(EntryColumns.GUID).append(Constants.DB_ARG);
                    }

                    String entryLinkString = ""; // don't set this to null as we need *some* value

                    if (mEntryLink != null && mEntryLink.length() > 0)
                    {
                        entryLinkString = mEntryLink.toString().trim();
                        if (mFeedBaseUrl != null && !entryLinkString.startsWith(Constants.HTTP_SCHEME) && !entryLinkString.startsWith(Constants.HTTPS_SCHEME)) {
                            entryLinkString = mFeedBaseUrl
                                    + (entryLinkString.startsWith(Constants.SLASH) ? entryLinkString : Constants.SLASH + entryLinkString);
                        }
                    }

                    String[] existenceValues = enclosureString != null ? (guidString != null ? new String[]{entryLinkString, enclosureString,
                            guidString} : new String[]{entryLinkString, enclosureString}) : (guidString != null ? new String[]{entryLinkString,
                            guidString} : new String[]{entryLinkString});

                    // First, try to update the feed
                    ContentResolver cr = MainApplication.getContext().getContentResolver();
                    boolean isUpdated = (!entryLinkString.isEmpty() || guidString != null)
                            && cr.update(mFeedEntriesUri, values, existenceStringBuilder.toString(), existenceValues) != 0;






                        // Insert it only if necessary
                       // any new post is arrived then insert
                        if (!isUpdated && !updateOnly)
                        {

                            Log.i(TAG,"Inserting New Post Id is : " +guidString);
                            Log.i(TAG,"Inserting Link  : " +entryLinkString );
                            Log.i(TAG,"Insert Post title  :" +improvedTitle);
                            Log.i(TAG,"Insert New Post Date : "+mEntryDate);
                            Log.i(TAG,"Insert New Post Date String Builder : : "+mDateStringBuilder);



                            // We put the date only for new entry (no need to change the past, you may already read it)
                            if (mEntryDate != null)
                            {
                                values.put(EntryColumns.DATE, mEntryDate.getTime());
                            } else {
                                values.put(EntryColumns.DATE, mNow--); // -1 to keep the good entries order
                            }

                            values.put(EntryColumns.LINK, entryLinkString);

                            // We cannot update, we need to insert it
                            mInsertedEntriesImages.add(imagesUrls);
                            mInserts.add(ContentProviderOperation.newInsert(mFeedEntriesUri).withValues(values).build());
                            mNewCount++;
                        }



                    // when any update post  is avaliable then update
                    if(isUpdated )
                    {
                        db = MainApplication.getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
                        Log.i(TAG,"Updated Post ID : "+guidString);
                        Log.i(TAG,"Updated Post Date  : " +mNewRealLastUpdate);
                        Log.i(TAG,"mEntryDate  : " + mEntryDate);
                        Log.i(TAG,"Updated Values Time : " + mEntryDate);
                        Log.i(TAG,"Updated Post DateString Builder : : "+mDateStringBuilder);

                        //db = MainApplication.getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
                        String select=EntryColumns.GUID +"=?";

                        if (mEntryDate != null)
                        {
                            values.put(EntryColumns.DATE, mEntryDate.getTime());
                        } /*else {
                            values.put(EntryColumns.DATE, mNow--); // -1 to keep the good entries order
                        }*/
                        values.put(EntryColumns.LINK, entryLinkString);

                        mInsertedEntriesImages.add(imagesUrls);

                        String flagvalue="yes";
                        values.put(EntryColumns.FLAG, flagvalue);

                        long l = db.delete(EntryColumns.TABLE_NAME,select,new String[]{guidString});
                        System.out.println("Delete" + l);
                        mInserts.add(ContentProviderOperation.newInsert(mFeedEntriesUri).withValues(values).build());

                       // System.out.println("Updated Values " + values);

                         db.close();



                    }


                    // No date, but we managed to update an entry => we already parsed the following entries and don't need to continue
                    if (isUpdated && mEntryDate == null)
                    {
                        cancel();
                    }
                }
            }
            else
            {
                cancel();
            }
            mDescription = null;
            mTitle = null;
            mEnclosure = null;
            mGuid = null;
            mAuthor = null;
        } else if (TAG_RSS.equals(localName) || TAG_RDF.equals(localName) || TAG_FEED.equals(localName)) {
            mDone = true;
        } else if (TAG_GUID.equals(localName)) {
            mGuidTagEntered = false;
        } else if (TAG_NAME.equals(localName) || TAG_AUTHOR.equals(localName) || TAG_CREATOR.equals(localName)) {
            mAuthorTagEntered = false;

            if (mTmpAuthor != null && mTmpAuthor.indexOf("@") == -1) { // no email
                if (mAuthor == null) {
                    mAuthor = new StringBuilder(mTmpAuthor);
                } else { // this indicates multiple authors
                    boolean found = false;
                    for (String previousAuthor : mAuthor.toString().split(",")) {
                        if (previousAuthor.equals(mTmpAuthor.toString())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mAuthor.append(Constants.COMMA_SPACE);
                        mAuthor.append(mTmpAuthor);
                    }
                }
            }

            mTmpAuthor = null;
        }


        // for check feeds are trash or deleted on server
        DeleteFeeds deleteFeeds = new DeleteFeeds();
        // get all post id from our sqlite database;
        ArrayList<String> mGetGuid = deleteFeeds.getPostId();
        ArrayList<String> mConvertedArray = null;


        // if array list is not empty
        if(mGetGuid.size() > 0)
        {
            // convert arraylist to json array
            JSONArray mGuidJsonArray = new JSONArray(mGetGuid);

            JSONObject mJsonObj = new JSONObject();

            try
            {
                mJsonObj.put("jsonarray", mGuidJsonArray);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            //   Log.i(TAG,"JsonObject To String  : "+mJsonObj.toString());


            // http request
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext httpContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost("http://web1.abmra.in/custom/GetDeletedGuid.php");

            try
            {

                // sending json Array to Server
                StringEntity se = new StringEntity(mJsonObj.toString());

                httpPost.setEntity(se);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");


                HttpResponse response = httpClient.execute(httpPost, httpContext); //execute your request and parse response
                HttpEntity entity = response.getEntity();

                String jsonString = EntityUtils.toString(entity); //if response in JSON format
                Log.i(TAG,"Json Response " + jsonString);
                // convert json Array to string array
                mConvertedArray = deleteFeeds.ConvertJsonarray(jsonString);


            } catch (Exception e)
            {
                e.printStackTrace();
            }

            // if Response Array is not null then delete
            if(mConvertedArray.size() > 0)
            {
                Log.i(TAG, "First Array : " +mGetGuid);
                Log.i(TAG, "Second Array : "+mConvertedArray);
                deleteFeeds.DeleteFeed(mConvertedArray);
            }
            // end of if loop
        }
    }


/*
    public class  DeleteFeeds
    {
        SQLiteDatabase mDatabase;
        // DatabaseHelper mDBHelper;


        // method for getting all postid(guid) from local database
        // return all post id in arraylist format
        private ArrayList<String> getPostId()
        {
            ArrayList<String> PostGuid = new ArrayList<String>();
            int _idd, _guid;
            String temp_guid;

            // mDBHelper = new DatabaseHelper(new Handler(),MainApplication.getContext());
            mDatabase = MainApplication.getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);

            String[] columns = new String[]{FeedData.EntryColumns._ID, FeedData.EntryColumns.GUID};
            String Table = FeedData.EntryColumns.TABLE_NAME;

            Cursor cursor = mDatabase.query(Table, columns, null, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
            {
                do {

                    _idd = cursor.getColumnIndex(FeedData.EntryColumns._ID);
                    _guid = cursor.getColumnIndex(FeedData.EntryColumns.GUID);
                    temp_guid = cursor.getString(_guid);
                    PostGuid.add(temp_guid);

                    //  Log.i(TAG,"Get Array of Post Id In RssParser : " + PostGuid);

                } while (cursor.moveToNext());


            } else
            {
                // Log.i(TAG, "Get Post Id In RssParser is Null ");
            }


            cursor.close();
            mDatabase.close();

            return PostGuid;
        }


        // convert response from php jsonArray to ListArray
        private  ArrayList<String> ConvertJsonarray(String mJsonArray) throws JSONException
        {
            JSONArray jsonArray = new JSONArray(mJsonArray);
            ArrayList<String> list = new ArrayList<String>();
            if(jsonArray !=null)
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    list.add(jsonArray.getString(i));
                }

            }

            return list;
            // Log.i(TAG,"Converted Response Json Array to List : "+list);
        }


        private void DeleteFeed(ArrayList<String> tempArray)
        {

            mDatabase = MainApplication.getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
            String args = TextUtils.join(", ", tempArray);
            mDatabase.execSQL(String.format("DELETE FROM entries WHERE guid IN (%s);", args));
            Log.i(TAG, "Deleted Array ");
            mDatabase.close();
                           */
/* long l = db.delete(EntryColumns.TABLE_NAME,select,new String[]{tempArray.get(i)});
                            Log.i(TAG,"Deleted Feed When Trash : "+tempArray.get(i));*//*



        }


    }
*/



    public String getFeedLink() {
        return mFeedLink;
    }

    public int getNewCount() {
        return mNewCount;
    }

    public boolean isDone() {
        return mDone;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    private void cancel() throws SAXException {
        if (!mCancelled) {
            mCancelled = true;
            mDone = true;
            endDocument();

            throw new SAXException("Finished");
        }
    }

    public void setFetchImages(boolean fetchImages) {
        this.mFetchImages = fetchImages;
    }

    private Date parseUpdateDate(String dateStr) {
        dateStr = improveDateString(dateStr);
        return parseUpdateDate(dateStr, true);
    }

    private Date parseUpdateDate(String dateStr, boolean tryAllFormat) {
        for (DateFormat format : UPDATE_DATE_FORMATS) {
            try {
                Date result = format.parse(dateStr);
                return (result.getTime() > mNow ? new Date(mNow) : result);
            } catch (ParseException ignored) {
            } // just do nothing
        }

        if (tryAllFormat)
            return parsePubdateDate(dateStr, false);
        else
            return null;
    }

    private Date parsePubdateDate(String dateStr) {
        dateStr = improveDateString(dateStr);
        return parsePubdateDate(dateStr, true);
    }

    private Date parsePubdateDate(String dateStr, boolean tryAllFormat) {
        for (DateFormat format : PUBDATE_DATE_FORMATS) {
            try {
                Date result = format.parse(dateStr);
                Log.i(TAG,"parsePubdateDate : "+result);
                return (result.getTime() > mNow ? new Date(mNow) : result);


            } catch (ParseException ignored) {
            } // just do nothing
        }

        if (tryAllFormat)
            return parseUpdateDate(dateStr, false);
        else
            return null;
    }

    private String improveDateString(String dateStr) {
        // We remove the first part if necessary (the day display)
        int coma = dateStr.indexOf(", ");
        ;
        if (coma != -1) {
            dateStr = dateStr.substring(coma + 2);
        }

        dateStr = dateStr.replaceAll("([0-9])T([0-9])", "$1 $2").replaceAll("Z$", " ").replaceAll("  ", " ").trim(); // fix useless char

        // Replace bad timezones
        for (String[] timezoneReplace : TIMEZONES_REPLACE)
        {
           /* Log.i(TAG,"TimeZoneReplace Array Values of Zero Imdex  : "+timezoneReplace[0]);
            Log.i(TAG,"TimeZoneReplace Array Values  of First Index : "+timezoneReplace[1]);*/
            dateStr = dateStr.replace(timezoneReplace[0], timezoneReplace[1]);
        }

        //Log.i(TAG,"DateString in ImproveDate : " +dateStr);
        return dateStr;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        // ignore warnings
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        // ignore errors
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        // ignore errors
    }

    @Override
    public void endDocument() throws SAXException {
        ContentResolver cr = MainApplication.getContext().getContentResolver();

        try {
            if (!mInserts.isEmpty()) {
                ContentProviderResult[] results = cr.applyBatch(FeedData.AUTHORITY, mInserts);

                if (mFetchImages)
                {
                    for (int i = 0; i < results.length; ++i) {
                        ArrayList<String> images = mInsertedEntriesImages.get(i);
                        if (images != null) {
                            FetcherService.addImagesToDownload(results[i].uri.getLastPathSegment(), images);
                        }
                    }
                }

                if (mRetrieveFullText) {
                    long[] entriesId = new long[results.length];
                    for (int i = 0; i < results.length; i++) {
                        entriesId[i] = Long.valueOf(results[i].uri.getLastPathSegment());
                    }

                    FetcherService.addEntriesToMobilize(entriesId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }

        ContentValues values = new ContentValues();
        if (mFeedName == null && mFeedTitle != null) {
            values.put(FeedColumns.NAME, mFeedTitle.trim());
        }
        values.putNull(FeedColumns.ERROR);
        values.put(FeedColumns.LAST_UPDATE, System.currentTimeMillis() - 3000); // by precaution to not miss some feeds
        values.put(FeedData.FeedColumns.REAL_LAST_UPDATE, mNewRealLastUpdate);
        cr.update(FeedColumns.CONTENT_URI(mId), values, null, null);

        super.endDocument();
    }

    private class FeedFilters
    {

        private final ArrayList<Rule> mFilters = new ArrayList<>();

        public FeedFilters(String feedId) {
            ContentResolver cr = MainApplication.getContext().getContentResolver();
            Cursor c = cr.query(FilterColumns.FILTERS_FOR_FEED_CONTENT_URI(feedId), new String[]{FilterColumns.FILTER_TEXT, FilterColumns.IS_REGEX,
                    FilterColumns.IS_APPLIED_TO_TITLE, FilterColumns.IS_ACCEPT_RULE}, null, null, null);
            while (c.moveToNext()) {
                Rule r = new Rule();
                r.filterText = c.getString(0);
                r.isRegex = c.getInt(1) == 1;
                r.isAppliedToTitle = c.getInt(2) == 1;
                r.isAcceptRule = c.getInt(3) == 1;
                mFilters.add(r);
            }
            c.close();

        }

        public boolean isEntryFiltered(String title, String content) {

            boolean isFiltered = false;

            for (Rule r : mFilters) {

                boolean isMatch = false;
                if (r.isRegex) {
                    Pattern p = Pattern.compile(r.filterText);
                    if (r.isAppliedToTitle) {
                        Matcher m = p.matcher(title);
                        isMatch = m.find();
                    } else if (content != null) {
                        Matcher m = p.matcher(content);
                        isMatch = m.find();
                    }
                } else if ((r.isAppliedToTitle && title.contains(r.filterText)) || (!r.isAppliedToTitle && content != null && content.contains(r.filterText))) {
                    isMatch = true;
                }

                if (r.isAcceptRule) {
                    if (isMatch) {
                        // accept rules override reject rules, the rest of the rules must be ignored
                        isFiltered = false;
                        break;
                    }
                } else if (isMatch) {
                    isFiltered = true;
                    // no break, there might be an accept rule later
                }
            }

            return isFiltered;
        }

        private class Rule {
            public String filterText;
            public boolean isRegex;
            public boolean isAppliedToTitle;
            public boolean isAcceptRule;
        }

    }




}
