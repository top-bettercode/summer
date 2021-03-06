package com.baidu.ueditor.hunter;

import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.MultiState;
import com.baidu.ueditor.define.State;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import top.bettercode.lang.util.ArrayUtil;
import top.bettercode.lang.util.FileUtil;

public class FileManager {

  private final String dir;
  private final String rootPath;
  private final String[] allowFiles;
  private final int count;

  public FileManager(Map<String, Object> conf) {

    this.rootPath = (String) conf.get("rootPath");
    this.dir = this.rootPath + conf.get("dir");
    this.allowFiles = this.getAllowFiles(conf.get("allowFiles"));
    this.count = (Integer) conf.get("count");

  }

  public State listFile(int index) {

    File dir = new File(this.dir);
    State state;

    if (!dir.exists()) {
      return new BaseState(false, AppInfo.NOT_EXIST);
    }

    if (!dir.isDirectory()) {
      return new BaseState(false, AppInfo.NOT_DIRECTORY);
    }

    Collection<File> list = FileUtil.listFiles(dir,
        pathname -> ArrayUtil.contains(allowFiles, pathname), true);

    if (index < 0 || index > list.size()) {
      state = new MultiState(true);
    } else {
      Object[] fileList = Arrays.copyOfRange(list.toArray(), index, index + this.count);
      state = this.getState(fileList);
    }

    state.putInfo("start", index);
    state.putInfo("total", list.size());

    return state;

  }

  private State getState(Object[] files) {

    MultiState state = new MultiState(true);
    BaseState fileState;

    File file;

    for (Object obj : files) {
      if (obj == null) {
        break;
      }
      file = (File) obj;
      fileState = new BaseState(true);
      fileState.putInfo("url", PathFormat.format(this.getPath(file)));
      state.addState(fileState);
    }

    return state;

  }

  private String getPath(File file) {

    String path = file.getAbsolutePath();

    return path.replace(this.rootPath, "/");

  }

  private String[] getAllowFiles(Object fileExt) {

    String[] exts;
    String ext;

    if (fileExt == null) {
      return new String[0];
    }

    exts = (String[]) fileExt;

    for (int i = 0, len = exts.length; i < len; i++) {

      ext = exts[i];
      exts[i] = ext.replace(".", "");

    }

    return exts;

  }

}
