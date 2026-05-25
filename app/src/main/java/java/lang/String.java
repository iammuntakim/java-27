package java.lang;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class String implements Serializable, Comparable<String>, CharSequence {

    private final java.lang.String backingString;

    public String() {
        this.backingString = "";
    }

    public String(java.lang.String value) {
        this.backingString = Objects.requireNonNull(value);
    }

    public String(char[] value) {
        this.backingString = new java.lang.String(value);
    }

    public String(char[] value, int offset, int count) {
        this.backingString = new java.lang.String(value, offset, count);
    }

    public String(int[] codePoints, int offset, int count) {
        this.backingString = new java.lang.String(codePoints, offset, count);
    }

    public String(byte[] bytes, int offset, int length, java.lang.String charsetName) throws UnsupportedEncodingException {
        this.backingString = new java.lang.String(bytes, offset, length, charsetName);
    }

    public String(byte[] bytes, java.lang.String charsetName) throws UnsupportedEncodingException {
        this.backingString = new java.lang.String(bytes, charsetName);
    }

    public String(byte[] bytes, int offset, int length, Charset charset) {
        this.backingString = new java.lang.String(bytes, offset, length, charset);
    }

    public String(byte[] bytes, Charset charset) {
        this.backingString = new java.lang.String(bytes, charset);
    }

    public String(byte[] bytes, int offset, int length) {
        this.backingString = new java.lang.String(bytes, offset, length);
    }

    public String(byte[] bytes) {
        this.backingString = new java.lang.String(bytes);
    }

    public String(StringBuffer buffer) {
        this.backingString = new java.lang.String(buffer);
    }

    public String(StringBuilder builder) {
        this.backingString = new java.lang.String(builder);
    }

    @Override
    public int length() {
        return backingString.length();
    }

    @Override
    public boolean isEmpty() {
        return backingString.isEmpty();
    }

    @Override
    public char charAt(int index) {
        return backingString.charAt(index);
    }

    public int codePointAt(int index) {
        return backingString.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return backingString.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return backingString.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return backingString.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        backingString.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public byte[] getBytes(java.lang.String charsetName) throws UnsupportedEncodingException {
        return backingString.getBytes(charsetName);
    }

    public byte[] getBytes(Charset charset) {
        return backingString.getBytes(charset);
    }

    public byte[] getBytes() {
        return backingString.getBytes();
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String) anObject;
            return backingString.equals(anotherString.backingString);
        }
        return false;
    }

    public boolean contentEquals(CharSequence cs) {
        return backingString.contentEquals(cs);
    }

    public boolean contentEquals(StringBuffer sb) {
        return backingString.contentEquals(sb);
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return (this == anotherString) ? true
                : (anotherString != null)
                && backingString.equalsIgnoreCase(anotherString.backingString);
    }

    @Override
    public int compareTo(String anotherString) {
        return backingString.compareTo(anotherString.backingString);
    }

    public int compareToIgnoreCase(String str) {
        return backingString.compareToIgnoreCase(str.backingString);
    }

    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        return backingString.regionMatches(toffset, other.backingString, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        return backingString.regionMatches(ignoreCase, toffset, other.backingString, ooffset, len);
    }

    public boolean startsWith(String prefix, int toffset) {
        return backingString.startsWith(prefix.backingString, toffset);
    }

    public boolean startsWith(String prefix) {
        return backingString.startsWith(prefix.backingString);
    }

    public boolean endsWith(String suffix) {
        return backingString.endsWith(suffix.backingString);
    }

    @Override
    public int hashCode() {
        return backingString.hashCode();
    }

    public int indexOf(int ch) {
        return backingString.indexOf(ch);
    }

    public int indexOf(int ch, int fromIndex) {
        return backingString.indexOf(ch, fromIndex);
    }

    public int lastIndexOf(int ch) {
        return backingString.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return backingString.lastIndexOf(ch, fromIndex);
    }

    public int indexOf(String str) {
        return backingString.indexOf(str.backingString);
    }

    public int indexOf(String str, int fromIndex) {
        return backingString.indexOf(str.backingString, fromIndex);
    }

    public int lastIndexOf(String str) {
        return backingString.lastIndexOf(str.backingString);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return backingString.lastIndexOf(str.backingString, fromIndex);
    }

    public String substring(int beginIndex) {
        return new String(backingString.substring(beginIndex));
    }

    public String substring(int beginIndex, int endIndex) {
        return new String(backingString.substring(beginIndex, endIndex));
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return backingString.subSequence(beginIndex, endIndex);
    }

    public String concat(String str) {
        return new String(backingString.concat(str.backingString));
    }

    public String replace(char oldChar, char newChar) {
        return new String(backingString.replace(oldChar, newChar));
    }

    public boolean matches(java.lang.String regex) {
        return backingString.matches(regex);
    }

    public boolean contains(CharSequence s) {
        return backingString.contains(s);
    }

    public String replaceFirst(java.lang.String regex, java.lang.String replacement) {
        return new String(backingString.replaceFirst(regex, replacement));
    }

    public String replaceAll(java.lang.String regex, java.lang.String replacement) {
        return new String(backingString.replaceAll(regex, replacement));
    }

    public String replace(CharSequence target, CharSequence replacement) {
        return new String(backingString.replace(target, replacement));
    }

    public String[] split(java.lang.String regex, int limit) {
        java.lang.String[] nativeArray = backingString.split(regex, limit);
        String[] wrappedArray = new String[nativeArray.length];
        for (int i = 0; i < nativeArray.length; i++) {
            wrappedArray[i] = new String(nativeArray[i]);
        }
        return wrappedArray;
    }

    public String[] split(java.lang.String regex) {
        return split(regex, 0);
    }

    public static String join(CharSequence delimiter, CharSequence... elements) {
        return new String(java.lang.String.join(delimiter, elements));
    }

    public String toLowerCase(Locale locale) {
        return new String(backingString.toLowerCase(locale));
    }

    public String toLowerCase() {
        return new String(backingString.toLowerCase());
    }

    public String toUpperCase(Locale locale) {
        return new String(backingString.toUpperCase(locale));
    }

    public String toUpperCase() {
        return new String(backingString.toUpperCase());
    }

    public String trim() {
        return new String(backingString.trim());
    }

    public String strip() {
        return new String(backingString.strip());
    }

    public String stripLeading() {
        return new String(backingString.stripLeading());
    }

    public String stripTrailing() {
        return new String(backingString.stripTrailing());
    }

    public boolean isBlank() {
        return backingString.isBlank();
    }

    public Stream<java.lang.String> lines() {
        return backingString.lines();
    }

    public String repeat(int count) {
        return new String(backingString.repeat(count));
    }

    @Override
    public java.lang.String toString() {
        return backingString;
    }

    @Override
    public IntStream chars() {
        return backingString.chars();
    }

    @Override
    public IntStream codePoints() {
        return backingString.codePoints();
    }

    public char[] toCharArray() {
        return backingString.toCharArray();
    }

    public static String valueOf(Object obj) {
        return new String(java.lang.String.valueOf(obj));
    }

    public static String valueOf(char[] data) {
        return new String(java.lang.String.valueOf(data));
    }

    public static String valueOf(char[] data, int offset, int count) {
        return new String(java.lang.String.valueOf(data, offset, count));
    }

    public static String copyValueOf(char[] data, int offset, int count) {
        return new String(java.lang.String.copyValueOf(data, offset, count));
    }

    public static String copyValueOf(char[] data) {
        return new String(java.lang.String.copyValueOf(data));
    }

    public static String valueOf(boolean b) {
        return new String(java.lang.String.valueOf(b));
    }

    public static String valueOf(char c) {
        return new String(java.lang.String.valueOf(c));
    }

    public static String valueOf(int i) {
        return new String(java.lang.String.valueOf(i));
    }

    public static String valueOf(long l) {
        return new String(java.lang.String.valueOf(l));
    }

    public static String valueOf(float f) {
        return new String(java.lang.String.valueOf(f));
    }

    public static String valueOf(double d) {
        return new String(java.lang.String.valueOf(d));
    }

    public String intern() {
        return new String(backingString.intern());
    }

    public java.lang.String getBackingString() {
        return this.backingString;
    }
}
