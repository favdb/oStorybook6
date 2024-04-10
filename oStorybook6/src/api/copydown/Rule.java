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

import api.jsoup.nodes.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

class Rule {
    private final Predicate<Node> filter;
    private final BiFunction<String, Node, String> replacement;

    public Supplier<String> getAppend() {
        return append;
    }

    private Supplier<String> append = null;

    private String name;
    public void setName(String name) {
        this.name = name;
    }

    Rule (String filter, BiFunction<String, Node, String> replacement) {
        this.filter = (el) -> el.nodeName().toLowerCase() == filter;
        this.replacement = replacement;
    }

    Rule (String[] filters, BiFunction<String, Node, String> replacement) {
        Set<String> availableFilters = new HashSet<>(Arrays.asList(filters));
        filter = (element -> availableFilters.contains(element.nodeName()));
        this.replacement = replacement;
    }
    Rule(Predicate<Node> filter, BiFunction<String, Node, String> replacement) {
        this.filter = filter;
        this.replacement = replacement;
    }
    Rule(Predicate<Node> filter, BiFunction<String, Node, String> replacement, Supplier<String> append) {
        this.filter = filter;
        this.replacement = replacement;
        this.append = append;
    }

    Predicate<Node> getFilter() {
        return filter;
    }

    BiFunction<String, Node, String> getReplacement() {
        return replacement;
    }
}
