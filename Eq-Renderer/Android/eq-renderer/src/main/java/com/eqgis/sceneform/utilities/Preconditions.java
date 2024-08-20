package com.eqgis.sceneform.utilities;

import androidx.annotation.Nullable;

/**
 * 条件检查工具类
 * @hide
 */
// 参考谷歌：/third_party/java_src/google_common/java7/java/com/google/common/base/Preconditions.java
public class Preconditions {
  /**
   * 非空检查
   * @param reference 被检查的对象
   * @throws NullPointerException 若为Null，则抛出异常
   */
  public static <T> T checkNotNull(@Nullable T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }

    return reference;
  }

  /**
   * 非空检查
   * @param reference 被检查的对象
   * @param errorMessage 错误信息
   * @throws NullPointerException 若为Null，则抛出异常
   */
  public static <T> T checkNotNull(@Nullable T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }

    return reference;
  }

  /**
   * 索引越界判断
   * @param index 索引
   * @param size 字符串长度、数组长度、集合大小
   * @throws IndexOutOfBoundsException 索引越界抛出异常
   * @throws IllegalArgumentException size为负时抛出异常
   */
  public static void checkElementIndex(int index, int size) {
    checkElementIndex(index, size, "index");
  }

  /**
   * 索引越界判断
   * @param index 索引
   * @param size 字符串长度、数组长度、集合大小
   * @param desc 描述错误信息的文本
   * @throws IndexOutOfBoundsException 索引越界抛出异常
   * @throws IllegalArgumentException size为负时抛出异常
   */
  public static void checkElementIndex(int index, int size, String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
    }
  }

  /**
   * 表达式判断
   * @param expression 表达式的返回值
   * @throws IllegalStateException 若表达式的返回值为false，则抛出异常
   */
  public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /**
   * 表达式判断
   * @param expression 表达式的返回值
   * @param errorMessage 错误信息
   * @throws IllegalStateException 若表达式的返回值为false，则抛出异常
   */
  public static void checkState(boolean expression, @Nullable Object errorMessage) {
    if (!expression) {
      throw new IllegalStateException(String.valueOf(errorMessage));
    }
  }

  private static String badElementIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index >= size
      return format("%s (%s) must be less than size (%s)", desc, index, size);
    }
  }

  /**
   * 格式转换
   */
  // Note that this is somewhat-improperly used from Verify.java as well.
  private static String format(String template, Object... args) {
    template = String.valueOf(template); // null -> "null"

    args = args == null ? new Object[] {"(Object[])null"} : args;

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template, templateStart, placeholderStart);
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template, templateStart, template.length());

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }
}
