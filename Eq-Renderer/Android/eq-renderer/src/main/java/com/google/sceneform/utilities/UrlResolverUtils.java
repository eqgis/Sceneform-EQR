package com.google.sceneform.utilities;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.util.function.Function;

/**
 * URI解析工具类
 * <p>
 *     用于处理资源路径解析和构建
 *     主要功能：根据父URI和相对路径，构建完整的资源URI
 * </p>
 */
public class UrlResolverUtils {

    /**
     * 获取缺失资源的完整URI
     * 此方法用于处理相对路径引用，将相对路径转换为基于父URI的完整URI
     *
     * @param parentUri 父URI，作为基础路径进行解析
     * @param missingResource 缺失资源的相对路径或标识符
     * @param urlResolver 可选的URL解析器函数，如果提供则优先使用此解析器
     * @return 解析后的完整URI
     * @throws AssertionError 如果missingResource包含scheme但应该是相对路径时抛出
     */
    public static Uri getUriFromMissingResource(
            @NonNull Uri parentUri,
            @NonNull String missingResource,
            @Nullable Function<String, Uri> urlResolver) {

        // 1. 优先使用自定义解析器（策略模式）
        // 如果提供了urlResolver，则直接使用它解析missingResource
        // 这允许调用者自定义解析逻辑，例如从不同位置加载资源
        if (urlResolver != null) {
            return urlResolver.apply(missingResource);
        }

        // 2. 标准化相对路径
        // 移除开头的斜杠，确保路径是相对路径格式
        // 例如："/images/icon.png" -> "images/icon.png"
        if (missingResource.startsWith("/")) {
            missingResource = missingResource.substring(1);
        }

        // 3. 解码和验证路径
        // 对路径进行URL解码，处理可能被编码的特殊字符
        Uri decodedMissingResUri = Uri.parse(Uri.decode(missingResource));

        // 安全性检查：确保路径不包含scheme（如http://, file://）
        // 因为missingResource应该是相对路径，不应该包含协议部分
        // 如果包含scheme，说明可能是绝对路径，与预期不符，抛出异常
        if (decodedMissingResUri.getScheme() != null) {
            throw new AssertionError(
                    String.format(
                            "Resource path contains a scheme but should be relative, uri: (%s)",
                            decodedMissingResUri));
        }

        // 4. 构建相对引用URI
        // 获取解码后的路径（不能为空）
        String decodedMissingResPath = Preconditions.checkNotNull(decodedMissingResUri.getPath());

        // 解码父URI，确保处理编码字符
        Uri decodedParentUri = Uri.parse(Uri.decode(parentUri.toString()));

        // 关键步骤：通过添加".."然后拼接缺失资源路径来构建相对引用
        // 示例：parentUri = "/folder1/folder2/"，missingResource = "../images/icon.png"
        // 构建结果："/folder1/folder2/../images/icon.png"
        Uri uri = decodedParentUri.buildUpon()
                .appendPath("..")
                .appendPath(decodedMissingResPath)
                .build();

        // 5. 规范化URI并返回
        // 使用Java标准URI类的normalize()方法处理路径中的".."和"."
        // 将上述示例规范化后得到："/folder1/images/icon.png"
        return Uri.parse(Uri.decode(URI.create(uri.toString()).normalize().toString()));
    }
}