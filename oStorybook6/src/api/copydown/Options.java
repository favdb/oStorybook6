/**
 * code from https://github.com/furstenheim/copy-down
 * 
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package api.copydown;

public class Options {
    final String br;
    final String hr;
    final String emDelimiter;
    final String strongDelimiter;
    final HeadingStyle headingStyle;
    final String bulletListMaker;
    final CodeBlockStyle codeBlockStyle;
    final LinkStyle linkStyle;
    final LinkReferenceStyle linkReferenceStyle;
    final String fence;

    public Options(String br, String hr, String emDelimiter, String strongDelimiter,
            HeadingStyle headingStyle, String bulletListMaker, CodeBlockStyle codeBlockStyle,
            LinkStyle linkStyle, LinkReferenceStyle linkReferenceStyle, String fence) {
        this.br = br;
        this.hr = hr;
        this.emDelimiter = emDelimiter;
        this.strongDelimiter = strongDelimiter;
        this.headingStyle = headingStyle;
        this.bulletListMaker = bulletListMaker;
        this.codeBlockStyle = codeBlockStyle;
        this.linkStyle = linkStyle;
        this.linkReferenceStyle = linkReferenceStyle;
        this.fence = fence;
    }
}
