package com.thanhlv.vizpro.ui.utils;

/**
 * Copyright 2018 Google LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;


/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_DRIVE_FOLDER = "application/vnd.google-apps.folder";
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";

    public DriveServiceHelper(Drive driveService) {

        mDriveService = driveService;
    }

    public static Drive getGoogleDriveService(Context context, GoogleSignInAccount account, String appName) {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        Drive googleDriveService =
                new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(appName)
                        .build();
        return googleDriveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */


    public Task<String> createFile(final String fileName) {
        return Tasks.call(mExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                File metadata = new File()
                        .setParents(Collections.singletonList("root"))
                        .setMimeType("text/plain")
                        .setName(fileName);

                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                return googleFile.getId();
            }
        });
    }

    public Task<String> createFile(final String fileName, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType("text/plain")
                        .setName(fileName);

                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                return googleFile.getId();
            }
        });
    }

    public Task<GoogleDriveFileHolder> createTextFile(final String fileName, final String content, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType("text/plain")
                        .setName(fileName);
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

                File googleFile = mDriveService.files().create(metadata, contentStream).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(googleFile.getId());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<GoogleDriveFileHolder> createTextFileIfNotExist(final String fileName, final String content, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

                FileList result = mDriveService.files().list()
                        .setQ("mimeType = 'text/plain' and name = '" + fileName + "' ")
                        .setSpaces("drive")
                        .execute();

                if (result.getFiles().size() > 0) {
                    googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                    return googleDriveFileHolder;
                } else {

                    List<String> root;
                    if (folderId == null) {
                        root = Collections.singletonList("root");
                    } else {

                        root = Collections.singletonList(folderId);
                    }

                    File metadata = new File()
                            .setParents(root)
                            .setMimeType("text/plain")
                            .setName(fileName);
                    ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

                    File googleFile = mDriveService.files().create(metadata, contentStream).execute();
                    if (googleFile == null) {
                        throw new IOException("Null result when requesting file creation.");
                    }

                    googleDriveFileHolder.setId(googleFile.getId());

                    return googleDriveFileHolder;
                }
            }
        });
    }

    public Task<GoogleDriveFileHolder> createFolder(final String folderName, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }
                File metadata = new File()
                        .setParents(root)
                        .setMimeType(DriveFolder.MIME_TYPE)
                        .setName(folderName);

                File googleFile = mDriveService.files().create(metadata).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }
                googleDriveFileHolder.setId(googleFile.getId());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<GoogleDriveFileHolder> createFolderIfNotExist(final String folderName, @Nullable final String parentFolderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                FileList result = mDriveService.files().list()
                        .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' ")
                        .setSpaces("drive")
                        .execute();

                if (result.getFiles().size() > 0) {
                    googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                    googleDriveFileHolder.setName(result.getFiles().get(0).getName());
//                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getCreatedTime().getValue());
//                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());

                    googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                    return googleDriveFileHolder;

                } else {

                    Log.d(TAG, "createFolderIfNotExist: not found");
                    List<String> root;
                    if (parentFolderId == null) {
                        root = Collections.singletonList("root");
                    } else {

                        root = Collections.singletonList(parentFolderId);
                    }
                    File metadata = new File()
                            .setParents(root)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setName(folderName);

                    File googleFile = mDriveService.files().create(metadata).execute();
                    if (googleFile == null) {
                        throw new IOException("Null result when requesting file creation.");
                    }
                    googleDriveFileHolder.setId(googleFile.getId());
                    return googleDriveFileHolder;
                }
            }
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(final String fileId) {
        return Tasks.call(mExecutor, new Callable<Pair<String, String>>() {
            @Override
            public Pair<String, String> call() throws Exception {
                // Retrieve the metadata as a File object.
                File metadata = mDriveService.files().get(fileId).execute();
                String name = metadata.getName();

                // Stream the file contents to a String.
                try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String contents = stringBuilder.toString();

                    return Pair.create(name, contents);
                }
            }
        });
    }

    public Task<Void> deleteFolderFile(final String fileId) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Retrieve the metadata as a File object.
                if (fileId != null) {
                    mDriveService.files().delete(fileId).execute();
                }

                return null;
            }
        });
    }

    public Task<GoogleDriveFileHolder> uploadFile(final File googleDiveFile, final AbstractInputStreamContent content) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.
                File fileMeta = mDriveService.files().create(googleDiveFile, content).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<GoogleDriveFileHolder> uploadFile(final java.io.File localFile, final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {
                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType(mimeType)
                        .setName(localFile.getName());

                FileContent fileContent = new FileContent(mimeType, localFile);

                File fileMeta = mDriveService.files().create(metadata, fileContent).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }

    public Task<List<GoogleDriveFileHolder>> searchFolder(final String folderName) {
        return Tasks.call(mExecutor, new Callable<List<GoogleDriveFileHolder>>() {
            @Override
            public List<GoogleDriveFileHolder> call() throws Exception {
                List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
                // Retrive the metadata as a File object.
                FileList result = mDriveService.files().list()
                        .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' ")
                        .setSpaces("drive")
                        .execute();

                for (int i = 0; i < result.getFiles().size(); i++) {

                    GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

                    googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                    googleDriveFileHolder.setName(result.getFiles().get(i).getName());

                    googleDriveFileHolderList.add(googleDriveFileHolder);
                }

                return googleDriveFileHolderList;
            }
        });
    }

    private static final String TAG = "DriveServiceHelper";

    public Task<List<GoogleDriveFileHolder>> searchFile(final String fileName, final String mimeType) {
        return Tasks.call(mExecutor, new Callable<List<GoogleDriveFileHolder>>() {
            @Override
            public List<GoogleDriveFileHolder> call() throws Exception {
                List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
                // Retrive the metadata as a File object.
                FileList result = mDriveService.files().list()
                        .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                        .setSpaces("drive")
                        .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                        .execute();


                for (int i = 0; i < result.getFiles().size(); i++) {
                    GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                    googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                    googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                    googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                    googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                    googleDriveFileHolderList.add(googleDriveFileHolder);

                }

                return googleDriveFileHolderList;
            }
        });
    }

    public Task<Void> downloadFile(final java.io.File fileSaveLocation, final String fileId) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Retrieve the metadata as a File object.
                OutputStream outputStream = new FileOutputStream(fileSaveLocation);
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return null;
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(final String fileId, final String name, final String content) {
        return Tasks.call(mExecutor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create a File containing any metadata changes.
                File metadata = new File().setName(name);

                // Convert content to an AbstractInputStreamContent instance.
                ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

                // Update the metadata and contents.
                mDriveService.files().update(fileId, metadata, contentStream).execute();
                return null;
            }
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<List<GoogleDriveFileHolder>> queryFiles() {
        return Tasks.call(mExecutor, new Callable<List<GoogleDriveFileHolder>>() {
                    @Override
                    public List<GoogleDriveFileHolder> call() throws Exception {
                        List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();


                        FileList result = mDriveService.files().list().setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType)").setSpaces("drive").execute();

                        for (int i = 0; i < result.getFiles().size(); i++) {

                            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                            googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                            googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                            if (result.getFiles().get(i).getSize() != null) {
                                googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                            }

                            if (result.getFiles().get(i).getModifiedTime() != null) {
                                googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                            }

                            if (result.getFiles().get(i).getCreatedTime() != null) {
                                googleDriveFileHolder.setCreatedTime(result.getFiles().get(i).getCreatedTime());
                            }

                            if (result.getFiles().get(i).getStarred() != null) {
                                googleDriveFileHolder.setStarred(result.getFiles().get(i).getStarred());
                            }

                            if (result.getFiles().get(i).getMimeType() != null) {
                                googleDriveFileHolder.setMimeType(result.getFiles().get(i).getMimeType());
                            }
                            googleDriveFileHolderList.add(googleDriveFileHolder);

                        }


                        return googleDriveFileHolderList;


                    }
                }
        );
    }

    public Task<List<GoogleDriveFileHolder>> queryFiles(@Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<List<GoogleDriveFileHolder>>() {
                    @Override
                    public List<GoogleDriveFileHolder> call() throws Exception {
                        List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
                        String parent = "root";
                        if (folderId != null) {
                            parent = folderId;
                        }

                        FileList result = mDriveService.files().list().setQ("'" + parent + "' in parents and trashed=false")
                                .setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType,thumbnailLink)")
                                .setSpaces("drive")
                                .execute();

                        for (int i = 0; i < result.getFiles().size(); i++) {

                            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                            googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                            googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                            if (result.getFiles().get(i).getSize() != null) {
                                googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                            }

                            if (result.getFiles().get(i).getModifiedTime() != null) {
                                googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                            }

                            if (result.getFiles().get(i).getCreatedTime() != null) {
                                googleDriveFileHolder.setCreatedTime(result.getFiles().get(i).getCreatedTime());
                            }

                            if (result.getFiles().get(i).getStarred() != null) {
                                googleDriveFileHolder.setStarred(result.getFiles().get(i).getStarred());
                            }

                            if (result.getFiles().get(i).getMimeType() != null) {
                                googleDriveFileHolder.setMimeType(result.getFiles().get(i).getMimeType());
                            }

                            if (!TextUtils.isEmpty(result.getFiles().get(i).getThumbnailLink())) {
                                googleDriveFileHolder.setThumbnailLink(result.getFiles().get(i).getThumbnailLink());
                            }

                            googleDriveFileHolderList.add(googleDriveFileHolder);

                        }


                        return googleDriveFileHolderList;


                    }
                }
        );
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            final ContentResolver contentResolver, final Uri uri) {
        return Tasks.call(mExecutor, new Callable<Pair<String, String>>() {
            @Override
            public Pair<String, String> call() throws Exception {
                // Retrieve the document's display name from its metadata.
                String name;
                try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        name = cursor.getString(nameIndex);
                    } else {
                        throw new IOException("Empty cursor returned for file.");
                    }
                }

                // Read the document's contents as a String.
                String content;
                try (InputStream is = contentResolver.openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    content = stringBuilder.toString();
                }

                return Pair.create(name, content);
            }
        });
    }
}