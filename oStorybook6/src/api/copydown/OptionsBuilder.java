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

public final class OptionsBuilder {

    private String br = "  ";
    private String hr = "* * *";
    private String emDelimiter = "_";
    private String strongDelimiter = "**";
    private HeadingStyle headingStyle = HeadingStyle.SETEXT;
    private String bulletListMaker = "*";
    private CopyDown.BlockStyle codeBlockStyle = CopyDown.BlockStyle.INDENTED;
    private LinkStyle linkStyle = LinkStyle.INLINED;
    private LinkReferenceStyle linkReferenceStyle = LinkReferenceStyle.DEFAULT;
    public String fence = "```";

    private OptionsBuilder() {
    }

    public static OptionsBuilder anOptions() {
	return new OptionsBuilder();
    }

    public OptionsBuilder withBr(String br) {
	this.br = br;
	return this;
    }

    public OptionsBuilder withHr(String hr) {
	this.hr = hr;
	return this;
    }

    public OptionsBuilder withEmDelimiter(String emDelimiter) {
	this.emDelimiter = emDelimiter;
	return this;
    }

    public OptionsBuilder withStrongDelimiter(String strongDelimiter) {
	this.strongDelimiter = strongDelimiter;
	return this;
    }

    public OptionsBuilder withHeadingStyle(HeadingStyle headingStyle) {
	this.headingStyle = headingStyle;
	return this;
    }

    public OptionsBuilder withBulletListMaker(String bulletListMaker) {
	this.bulletListMaker = bulletListMaker;
	return this;
    }

    public OptionsBuilder withCodeBlockStyle(CopyDown.BlockStyle codeBlockStyle) {
	this.codeBlockStyle = codeBlockStyle;
	return this;
    }

    public OptionsBuilder withLinkStyle(LinkStyle linkStyle) {
	this.linkStyle = linkStyle;
	return this;
    }

    public OptionsBuilder withLinkReferenceStyle(LinkReferenceStyle linkReferenceStyle) {
	this.linkReferenceStyle = linkReferenceStyle;
	return this;
    }

    public OptionsBuilder withFence(String fence) {
	this.fence = fence;
	return this;
    }

    public Options build() {
	return new Options(br, hr, emDelimiter, strongDelimiter, headingStyle, bulletListMaker, codeBlockStyle,
		linkStyle, linkReferenceStyle, fence);
    }
}
