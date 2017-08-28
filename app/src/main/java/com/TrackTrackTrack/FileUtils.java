/*
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import android.os.Environment;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author JAYMIN LEE
 * @version 1.1
 * 
 * 파일 폴더를 생성하는 작업을 담당하는 클래스이다. 
 * 폴더생성은 SD Card의 유무, 사용가능 형태, 중복처리 등 예외 상황이 많이 있기 떄문에
 * 새로은 클래스를 만들어 디자인 하였다.
 */
public class FileUtils {
  /**
   * SD card는 FAT32 파일 포멧을 채택하고 있다.
   */
  private static final int MAX_FILENAME_LENGTH = 260;

  public static final String SDCARD_TOP_DIR = "TrackTrackTrack";
  
  /**
   * 파일 이름에 사용 되어서는 안될 첫 글자들을 정의 한다.
   */
  private static final Pattern PROHIBITED_CHAR_PATTERN =
      Pattern.compile("[^ A-Za-z0-9_.()]+");

  /**
   * SD 카드의 절대 경로를 얻어온다음
   * 최 외각 폴더에 TrackTrackTrack를 생성한다.
   * 그 다음 금지 단어를 필터링 하여 정상적인 폴더를 생성 한다.
   */
  public String buildExternalDirectoryPath(String... components) {
    StringBuilder dirNameBuilder = new StringBuilder();
    dirNameBuilder.append(Environment.getExternalStorageDirectory());
    dirNameBuilder.append(File.separatorChar);
    dirNameBuilder.append(SDCARD_TOP_DIR);
    for (String component : components) {
      dirNameBuilder.append(File.separatorChar);
      dirNameBuilder.append(component);
    }
    return dirNameBuilder.toString();
  }

  /**
   * SD 카드가 사용가느한 상태인지를 점검 한다.
   */
  public boolean isSdCardAvailable() {
    return Environment.MEDIA_MOUNTED.equals(
        Environment.getExternalStorageState());
  }

  /**
   * 금지단어가 있을경우 해당 단어를 제거 하는 함수 이다.
   * FAT32 파일 포멧에서는 최대 260자 까지 파일 이름으로 올 수 있다.
   */
  String sanitizeName(String name) {
    String cleaned = PROHIBITED_CHAR_PATTERN.matcher(name).replaceAll("");

    return (cleaned.length() > MAX_FILENAME_LENGTH)
        ? cleaned.substring(0, MAX_FILENAME_LENGTH)
        : cleaned.toString();
  }

  /**
   * 디텔토리가 있으면 생성하지 않고
   * 없다면 생성한다.
   * 둘다 아니면 디렉토리 생성 실패를 리턴 한다.
   */
  public boolean ensureDirectoryExists(File dir) {
    if (dir.exists() && dir.isDirectory()) {
      return true;
    }

    if (dir.mkdirs()) {
      return true;
    }

    return false;
  }

  /**
   * 파일 이름을 생성 하기전에 해당 폴더에 같은 파일이 있는지를 판별한다.
   * 유니크 하지 않다면 뒤에 접미사를 자동으로 붙여주어 파일을 구분 할 수 있도록 한다.
   *
   * @param 파일이 생성 되어질 디렉토리 경로
   * @param 유니크 한지 여부를 알 수 없는 파일이름
   * @param 파일 확장자 ( . gpx )
   * @return 디렉토리 이름을 제외한 유니크한 파일 이름
   */
  public synchronized String buildUniqueFileName(File directory,
      String fileBaseName, String extension) {
    return buildUniqueFileName(directory, fileBaseName, extension, 0);
  }

  /**
   * 접두어 파일이름 즉 사용자가 입력한 파일이름이 유니크 하지 않다면 접미어로 (1) (2) .. (?) 까지의 숫자를
   * 붙여서 유니크한 파일이름과 .gpx의 확장자를 결정하여 준다.
   *
   */
  private String buildUniqueFileName(File directory, String fileBaseName,
      String extension, int suffix) {
    String suffixedBaseName = fileBaseName;
    if (suffix > 0) {
      suffixedBaseName += " (" + Integer.toString(suffix) + ")";
    }

    String fullName = suffixedBaseName + "." + extension;
    String sanitizedName = sanitizeName(fullName);
    if (!fileExists(directory, sanitizedName)) {
      return sanitizedName;
    }

    return buildUniqueFileName(directory, fileBaseName, extension, suffix + 1);
  }

  /**
   * GPX 포멧 파일이 덮어 씌워지는 것을 방지하기위해 기존의 이름이 있는지를 정확히 체크 하여야 한다.
   * 그래야 기존의 파일드를 보호 할 수 있다.
   */
  protected boolean fileExists(File directory, String fullName) {
    File file = new File(directory, fullName);
    return file.exists();
  }
}
