package com.supermarket.backend.util;

import org.springframework.util.StringUtils;

/**
 * 将规格说明中的中文计量单位统一为常用英文缩写，避免「500ml」与「500毫升」并存。
 */
public final class SpecificationUnitNormalizer {

    private SpecificationUnitNormalizer() {}

    /**
     * 规范化规格文字：去多余空白，并将数量+中文单位转为标准写法（如 500毫升 → 500ml）。
     */
    public static String normalize(String specification) {
        if (!StringUtils.hasText(specification)) {
            return specification == null ? null : specification.trim();
        }
        String s = specification.trim().replaceAll("\\s+", " ");

        // 先匹配较长单位，避免「毫升」被「升」误伤
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*毫升", "$1ml");
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*升", "$1L");
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*千克", "$1kg");
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*公斤", "$1kg");
        // 「克」需排除「千克」「克拉」
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*克(?!拉)", "$1g");

        // 已写英文单位时统一间隔与大小写：500 M L → 500ml
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*m\\s*l(?![a-z])", "$1ml");
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*kg(?![a-z])", "$1kg");
        s = s.replaceAll("(?iu)(\\d+(?:\\.\\d+)?)\\s*g(?![a-z])", "$1g");

        return s;
    }
}
