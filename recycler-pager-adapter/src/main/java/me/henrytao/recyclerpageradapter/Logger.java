/*
 * Copyright 2016 "Henry Tao <hi@henrytao.me>"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.henrytao.recyclerpageradapter;

import android.util.Log;

/**
 * Created by henrytao on 11/13/15.
 */
public final class Logger {

  private static final String DEFAULT_TAG = "Logger";

  public static Logger newInstance(Logger.LogLevel level) {
    return new Logger("Logger", level);
  }

  public static Logger newInstance(String tag, Logger.LogLevel logLevel) {
    return new Logger(tag, logLevel);
  }

  public final Logger.LogLevel mLogLevel;

  private final String mTag;

  protected Logger(String tag, Logger.LogLevel logLevel) {
    this.mTag = tag;
    this.mLogLevel = logLevel;
  }

  public void d(String format, Object... args) {
    if (this.shouldLog(Logger.LogLevel.DEBUG)) {
      Log.d(this.mTag, String.format(format, args));
    }
  }

  public void e(Throwable error) {
    this.e(error, "", new Object[0]);
  }

  public void e(String format, Object... args) {
    this.e((Throwable) null, format, args);
  }

  public void e(Throwable error, String format, Object... args) {
    if (this.shouldLog(Logger.LogLevel.ERROR)) {
      Log.e(this.mTag, String.format(format, args), error);
    }
  }

  public void i(String format, Object... args) {
    if (this.shouldLog(Logger.LogLevel.INFO)) {
      Log.i(this.mTag, String.format(format, args));
    }
  }

  public void v(String format, Object... args) {
    if (this.shouldLog(Logger.LogLevel.VERBOSE)) {
      Log.v(this.mTag, String.format(format, args));
    }
  }

  public void w(String format, Object... args) {
    if (this.shouldLog(Logger.LogLevel.WARN)) {
      Log.w(this.mTag, String.format(format, args));
    }
  }

  private boolean shouldLog(Logger.LogLevel level) {
    return this.mLogLevel.toInt() >= level.toInt();
  }

  public enum LogLevel {
    NONE(0),
    ERROR(1),
    WARN(2),
    INFO(3),
    DEBUG(4),
    VERBOSE(5);

    public static Logger.LogLevel valueOf(int value) {
      return value == ERROR.toInt() ? ERROR : (value == WARN.toInt() ? WARN
          : (value == INFO.toInt() ? INFO : (value == DEBUG.toInt() ? DEBUG : (value == VERBOSE.toInt() ? VERBOSE : NONE))));
    }

    private final int mValue;

    LogLevel(int value) {
      this.mValue = value;
    }

    public int toInt() {
      return this.mValue;
    }
  }
}