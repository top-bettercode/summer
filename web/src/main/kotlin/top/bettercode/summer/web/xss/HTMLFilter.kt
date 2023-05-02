package top.bettercode.summer.web.xss

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * HTML filtering utility for protecting against XSS (Cross Site Scripting).
 *
 *
 * This code is licensed LGPLv3
 *
 *
 * This code is a Java port of the original work in PHP by Cal Hender[*
 * http://code.iamcal.com/php/lib](sen. )_filter/
 *
 *
 * The trickiest part of the translation was handling the differences in regex handling between PHP
 * and Java.  These resources were helpful in the pro[* 
 *
 *
 * http://java.sun.com/j2se/1.4.2/docs/api/java/util/re](cess: )gex/[*
 * http://us2.php.net/manual/en/reference.pcre.patte](Pattern.html )rn.m[*
 * http://www.regular-expressions.inf](odifiers.php )o/modifiers.html
 *
 *
 * A note on naming conventions: instance variables are prefixed with a "v"; global constants are in
 * all caps.
 *
 *
 * Sample use: String input = ... String clean = new HTMLFilter().filter( input );
 *
 *
 * The class is not thread safe. Create a new instance if in doubt.
 *
 *
 * If you find bugs or have suggestions on improvement (especially regarding performance), please
 * contact us.  The latest version of this source, and our c[details, can be
 * found](ontact)at [...](http://xss-html-filter.sf.net)
 *
 * @author Joseph O'Connell
 * @author Cal Hendersen
 * @author Michael Semb Wever
 */
class HTMLFilter {
    /**
     * set of allowed html elements, along with allowed attributes for each element
     */
    private val vAllowed: MutableMap<String, List<String>>

    /**
     * counts of open tags for each (allowable) html element
     */
    private val vTagCounts: MutableMap<String, Int> = HashMap()

    /**
     * html elements which must always be self-closing (e.g. "<img></img>")
     */
    private val vSelfClosingTags: Array<String>?

    /**
     * html elements which must always have separate opening and closing tags (e.g. "****")
     */
    private val vNeedClosingTags: Array<String>?

    /**
     * set of disallowed html elements
     */
    private val vDisallowed: Array<String>?

    /**
     * attributes which should be checked for valid protocols
     */
    private val vProtocolAtts: Array<String>?

    /**
     * allowed protocols
     */
    private val vAllowedProtocols: Array<String>?

    /**
     * tags which should be removed if they contain no content (e.g. "****" or "****")
     */
    private val vRemoveBlanks: Array<String>?

    /**
     * entities allowed within html markup
     */
    private val vAllowedEntities: Array<String>?

    /**
     * flag determining whether comments are allowed in input String.
     */
    val isStripComments: Boolean
    private val encodeQuotes: Boolean

    /**
     * flag determining whether to try to make tags when presented with "unbalanced" angle brackets
     * (e.g. "**** " becomes "** text **").  If set to false, unbalanced angle brackets will
     * be html escaped.
     */
    val isAlwaysMakeTags: Boolean
    private var vDebug = false

    /**
     * Default constructor.
     */
    constructor() {
        vAllowed = HashMap()
        val a_atts = ArrayList<String>()
        a_atts.add("href")
        a_atts.add("target")
        vAllowed["a"] = a_atts
        val img_atts = ArrayList<String>()
        img_atts.add("src")
        img_atts.add("width")
        img_atts.add("height")
        img_atts.add("alt")
        vAllowed["img"] = img_atts
        val no_atts = ArrayList<String>()
        vAllowed["b"] = no_atts
        vAllowed["strong"] = no_atts
        vAllowed["i"] = no_atts
        vAllowed["em"] = no_atts
        vSelfClosingTags = arrayOf("img")
        vNeedClosingTags = arrayOf("a", "b", "strong", "i", "em")
        vDisallowed = arrayOf()
        vAllowedProtocols = arrayOf("http", "mailto", "https") // no ftp.
        vProtocolAtts = arrayOf("src", "href")
        vRemoveBlanks = arrayOf("a", "b", "strong", "i", "em")
        vAllowedEntities = arrayOf("amp", "gt", "lt", "quot")
        isStripComments = true
        encodeQuotes = true
        isAlwaysMakeTags = true
    }

    /**
     * Set debug flag to true. Otherwise use default settings. See the default constructor.
     *
     * @param debug turn debug on with a true argument
     */
    constructor(debug: Boolean) : this() {
        vDebug = debug
    }

    /**
     * Map-parameter configurable constructor.
     *
     * @param conf map containing configuration. keys match field names.
     */
    constructor(conf: Map<String?, Any?>) {
        assert(conf.containsKey("vAllowed")) { "configuration requires vAllowed" }
        assert(conf.containsKey("vSelfClosingTags")) { "configuration requires vSelfClosingTags" }
        assert(conf.containsKey("vNeedClosingTags")) { "configuration requires vNeedClosingTags" }
        assert(conf.containsKey("vDisallowed")) { "configuration requires vDisallowed" }
        assert(conf.containsKey("vAllowedProtocols")) { "configuration requires vAllowedProtocols" }
        assert(conf.containsKey("vProtocolAtts")) { "configuration requires vProtocolAtts" }
        assert(conf.containsKey("vRemoveBlanks")) { "configuration requires vRemoveBlanks" }
        assert(conf.containsKey("vAllowedEntities")) { "configuration requires vAllowedEntities" }
        vAllowed = Collections.unmodifiableMap(conf["vAllowed"] as HashMap<String, List<String>>?)
        vSelfClosingTags = conf["vSelfClosingTags"] as Array<String>?
        vNeedClosingTags = conf["vNeedClosingTags"] as Array<String>?
        vDisallowed = conf["vDisallowed"] as Array<String>?
        vAllowedProtocols = conf["vAllowedProtocols"] as Array<String>?
        vProtocolAtts = conf["vProtocolAtts"] as Array<String>?
        vRemoveBlanks = conf["vRemoveBlanks"] as Array<String>?
        vAllowedEntities = conf["vAllowedEntities"] as Array<String>?
        isStripComments = if (conf.containsKey("stripComment")) (conf["stripComment"] as Boolean?)!! else true
        encodeQuotes = if (conf.containsKey("encodeQuotes")) (conf["encodeQuotes"] as Boolean?)!! else true
        isAlwaysMakeTags = if (conf.containsKey("alwaysMakeTags")) (conf["alwaysMakeTags"] as Boolean?)!! else true
    }

    //---------------------------------------------------------------
    private fun reset() {
        vTagCounts.clear()
    }

    private fun debug(msg: String) {
        if (vDebug) {
            Logger.getAnonymousLogger().info(msg)
        }
    }

    /**
     * given a user submitted input String, filter out any invalid or restricted html.
     *
     * @param input text (i.e. submitted by a user) than may contain html
     * @return "clean" version of input, with only valid, whitelisted html elements allowed
     */
    fun filter(input: String?): String {
        reset()
        var s = input
        debug("************************************************")
        debug("              INPUT: $input")
        s = escapeComments(s)
        debug("     escapeComments: $s")
        s = balanceHTML(s)
        debug("        balanceHTML: $s")
        s = checkTags(s)
        debug("          checkTags: $s")
        s = processRemoveBlanks(s)
        debug("processRemoveBlanks: $s")
        s = validateEntities(s)
        debug("    validateEntites: $s")
        debug("************************************************\n\n")
        return s
    }

    private fun escapeComments(s: String?): String {
        val m = P_COMMENTS.matcher(s)
        val buf = StringBuffer()
        if (m.find()) {
            val match = m.group(1) //(.*?)
            m.appendReplacement(buf, Matcher.quoteReplacement("<!--" + htmlSpecialChars(match) + "-->"))
        }
        m.appendTail(buf)
        return buf.toString()
    }

    private fun balanceHTML(s: String): String {
        var s1 = s
        if (isAlwaysMakeTags) {
            //
            // try and form html
            //
            s1 = regexReplace(P_END_ARROW, "", s1)
            s1 = regexReplace(P_BODY_TO_END, "<$1>", s1)
            s1 = regexReplace(P_XML_CONTENT, "$1<$2", s1)
        } else {
            //
            // escape stray brackets
            //
            s1 = regexReplace(P_STRAY_LEFT_ARROW, "&lt;$1", s1)
            s1 = regexReplace(P_STRAY_RIGHT_ARROW, "$1$2&gt;<", s1)

            //
            // the last regexp causes '<>' entities to appear
            // (we need to do a lookahead assertion so that the last bracket can
            // be used in the next pass of the regexp)
            //
            s1 = regexReplace(P_BOTH_ARROWS, "", s1)
        }
        return s1
    }

    private fun checkTags(s: String): String {
        var s1 = s
        val m = P_TAGS.matcher(s1)
        val buf = StringBuffer()
        while (m.find()) {
            var replaceStr = m.group(1)
            replaceStr = processTag(replaceStr)
            m.appendReplacement(buf, Matcher.quoteReplacement(replaceStr))
        }
        m.appendTail(buf)

        // these get tallied in processTag
        // (remember to reset before subsequent calls to filter method)
        val sBuilder = StringBuilder(buf.toString())
        for (key in vTagCounts.keys) {
            for (ii in 0 until vTagCounts[key]!!) {
                sBuilder.append("</").append(key).append(">")
            }
        }
        s1 = sBuilder.toString()
        return s1
    }

    private fun processRemoveBlanks(s: String): String {
        var result = s
        for (tag in vRemoveBlanks!!) {
            if (!P_REMOVE_PAIR_BLANKS.containsKey(tag)) {
                P_REMOVE_PAIR_BLANKS
                        .putIfAbsent(tag, Pattern.compile("<$tag(\\s[^>]*)?></$tag>"))
            }
            result = regexReplace(P_REMOVE_PAIR_BLANKS[tag], "", result)
            if (!P_REMOVE_SELF_BLANKS.containsKey(tag)) {
                P_REMOVE_SELF_BLANKS.putIfAbsent(tag, Pattern.compile("<$tag(\\s[^>]*)?/>"))
            }
            result = regexReplace(P_REMOVE_SELF_BLANKS[tag], "", result)
        }
        return result
    }

    private fun processTag(s: String): String {
        // ending tags
        var m = P_END_TAG.matcher(s)
        if (m.find()) {
            val name = m.group(1).lowercase(Locale.getDefault())
            if (allowed(name)) {
                if (!inArray(name, vSelfClosingTags)) {
                    if (vTagCounts.containsKey(name)) {
                        vTagCounts[name] = vTagCounts[name]!! - 1
                        return "</$name>"
                    }
                }
            }
        }

        // starting tags
        m = P_START_TAG.matcher(s)
        if (m.find()) {
            val name = m.group(1).lowercase(Locale.getDefault())
            val body = m.group(2)
            var ending = m.group(3)

            //debug( "in a starting tag, name='" + name + "'; body='" + body + "'; ending='" + ending + "'" );
            return if (allowed(name)) {
                val params = StringBuilder()
                val m2 = P_QUOTED_ATTRIBUTES.matcher(body)
                val m3 = P_UNQUOTED_ATTRIBUTES.matcher(body)
                val paramNames: MutableList<String> = ArrayList()
                val paramValues: MutableList<String> = ArrayList()
                while (m2.find()) {
                    paramNames.add(m2.group(1)) //([a-z0-9]+)
                    paramValues.add(m2.group(3)) //(.*?)
                }
                while (m3.find()) {
                    paramNames.add(m3.group(1)) //([a-z0-9]+)
                    paramValues.add(m3.group(3)) //([^\"\\s']+)
                }
                var paramName: String
                var paramValue: String
                for (ii in paramNames.indices) {
                    paramName = paramNames[ii].lowercase(Locale.getDefault())
                    paramValue = paramValues[ii]

//          debug( "paramName='" + paramName + "'" );
//          debug( "paramValue='" + paramValue + "'" );
//          debug( "allowed? " + vAllowed.get( name ).contains( paramName ) );
                    if (allowedAttribute(name, paramName)) {
                        if (inArray(paramName, vProtocolAtts)) {
                            paramValue = processParamProtocol(paramValue)
                        }
                        params.append(" ").append(paramName).append("=\"").append(paramValue).append("\"")
                    }
                }
                if (inArray(name, vSelfClosingTags)) {
                    ending = " /"
                }
                if (inArray(name, vNeedClosingTags)) {
                    ending = ""
                }
                if (ending == null || ending.length < 1) {
                    if (vTagCounts.containsKey(name)) {
                        vTagCounts[name] = vTagCounts[name]!! + 1
                    } else {
                        vTagCounts[name] = 1
                    }
                } else {
                    ending = " /"
                }
                "<$name$params$ending>"
            } else {
                ""
            }
        }

        // comments
        m = P_COMMENT.matcher(s)
        return if (!isStripComments && m.find()) {
            "<" + m.group() + ">"
        } else ""
    }

    private fun processParamProtocol(str: String): String {
        var s = str
        s = decodeEntities(s)
        val m = P_PROTOCOL.matcher(s)
        if (m.find()) {
            val protocol = m.group(1)
            if (!inArray(protocol, vAllowedProtocols)) {
                // bad protocol, turn into local anchor link instead
                s = "#" + s.substring(protocol.length + 1)
                if (s.startsWith("#//")) {
                    s = "#" + s.substring(3)
                }
            }
        }
        return s
    }

    private fun decodeEntities(s: String): String {
        var s1 = s
        var buf = StringBuffer()
        var m = P_ENTITY.matcher(s1)
        while (m.find()) {
            val match = m.group(1)
            val decimal = Integer.decode(match)
            m.appendReplacement(buf, Matcher.quoteReplacement(chr(decimal)))
        }
        m.appendTail(buf)
        s1 = buf.toString()
        buf = StringBuffer()
        m = P_ENTITY_UNICODE.matcher(s1)
        while (m.find()) {
            val match = m.group(1)
            val decimal = Integer.valueOf(match, 16)
            m.appendReplacement(buf, Matcher.quoteReplacement(chr(decimal)))
        }
        m.appendTail(buf)
        s1 = buf.toString()
        buf = StringBuffer()
        m = P_ENCODE.matcher(s1)
        while (m.find()) {
            val match = m.group(1)
            val decimal = Integer.valueOf(match, 16)
            m.appendReplacement(buf, Matcher.quoteReplacement(chr(decimal)))
        }
        m.appendTail(buf)
        s1 = buf.toString()
        s1 = validateEntities(s1)
        return s1
    }

    private fun validateEntities(s: String?): String {
        val buf = StringBuffer()

        // validate entities throughout the string
        val m = P_VALID_ENTITIES.matcher(s)
        while (m.find()) {
            val one = m.group(1) //([^&;]*)
            val two = m.group(2) //(?=(;|&|$))
            m.appendReplacement(buf, Matcher.quoteReplacement(checkEntity(one, two)))
        }
        m.appendTail(buf)
        return encodeQuotes(buf.toString())
    }

    private fun encodeQuotes(s: String): String {
        return if (encodeQuotes) {
            val buf = StringBuffer()
            val m = P_VALID_QUOTES.matcher(s)
            while (m.find()) {
                val one = m.group(1) //(>|^)
                val two = m.group(2) //([^<]+?)
                val three = m.group(3) //(<|$)
                m.appendReplacement(buf,
                        Matcher.quoteReplacement(one + regexReplace(P_QUOTE, "&quot;", two) + three))
            }
            m.appendTail(buf)
            buf.toString()
        } else {
            s
        }
    }

    private fun checkEntity(preamble: String, term: String): String {
        return if (";" == term && isValidEntity(preamble)) "&$preamble" else "&amp;$preamble"
    }

    private fun isValidEntity(entity: String): Boolean {
        return inArray(entity, vAllowedEntities)
    }

    private fun allowed(name: String): Boolean {
        return (vAllowed.isEmpty() || vAllowed.containsKey(name)) && !inArray(name, vDisallowed)
    }

    private fun allowedAttribute(name: String, paramName: String): Boolean {
        return allowed(name) && (vAllowed.isEmpty() || vAllowed[name]!!.contains(paramName))
    }

    companion object {
        /**
         * regex flag union representing /si modifiers in php
         */
        private const val REGEX_FLAGS_SI = Pattern.CASE_INSENSITIVE or Pattern.DOTALL
        private val P_COMMENTS = Pattern.compile("<!--(.*?)-->", Pattern.DOTALL)
        private val P_COMMENT = Pattern.compile("^!--(.*)--$", REGEX_FLAGS_SI)
        private val P_TAGS = Pattern.compile("<(.*?)>", Pattern.DOTALL)
        private val P_END_TAG = Pattern.compile("^/([a-z0-9]+)", REGEX_FLAGS_SI)
        private val P_START_TAG = Pattern
                .compile("^([a-z0-9]+)(.*?)(/?)$", REGEX_FLAGS_SI)
        private val P_QUOTED_ATTRIBUTES = Pattern
                .compile("([a-z0-9]+)=([\"'])(.*?)\\2", REGEX_FLAGS_SI)
        private val P_UNQUOTED_ATTRIBUTES = Pattern
                .compile("([a-z0-9]+)(=)([^\"\\s']+)", REGEX_FLAGS_SI)
        private val P_PROTOCOL = Pattern.compile("^([^:]+):", REGEX_FLAGS_SI)
        private val P_ENTITY = Pattern.compile("&#(\\d+);?")
        private val P_ENTITY_UNICODE = Pattern.compile("&#x([0-9a-f]+);?")
        private val P_ENCODE = Pattern.compile("%([0-9a-f]{2});?")
        private val P_VALID_ENTITIES = Pattern.compile("&([^&;]*)(?=(;|&|$))")
        private val P_VALID_QUOTES = Pattern
                .compile("(>|^)([^<]+?)(<|$)", Pattern.DOTALL)
        private val P_END_ARROW = Pattern.compile("^>")
        private val P_BODY_TO_END = Pattern.compile("<([^>]*?)(?=<|$)")
        private val P_XML_CONTENT = Pattern.compile("(^|>)([^<]*?)(?=>)")
        private val P_STRAY_LEFT_ARROW = Pattern.compile("<([^>]*?)(?=<|$)")
        private val P_STRAY_RIGHT_ARROW = Pattern.compile("(^|>)([^<]*?)(?=>)")
        private val P_AMP = Pattern.compile("&")
        private val P_QUOTE = Pattern.compile("<")
        private val P_LEFT_ARROW = Pattern.compile("<")
        private val P_RIGHT_ARROW = Pattern.compile(">")
        private val P_BOTH_ARROWS = Pattern.compile("<>")

        // @xxx could grow large... maybe use sesat's ReferenceMap
        private val P_REMOVE_PAIR_BLANKS: ConcurrentMap<String, Pattern> = ConcurrentHashMap()
        private val P_REMOVE_SELF_BLANKS: ConcurrentMap<String, Pattern> =   ConcurrentHashMap()

        //---------------------------------------------------------------
        // my versions of some PHP library functions
        fun chr(decimal: Int): String {
            return decimal.toChar().toString()
        }

        fun htmlSpecialChars(s: String): String {
            var result = s
            result = regexReplace(P_AMP, "&amp;", result)
            result = regexReplace(P_QUOTE, "&quot;", result)
            result = regexReplace(P_LEFT_ARROW, "&lt;", result)
            result = regexReplace(P_RIGHT_ARROW, "&gt;", result)
            return result
        }

        private fun regexReplace(regexPattern: Pattern?, replacement: String,
                                 s: String): String {
            val m = regexPattern!!.matcher(s)
            return m.replaceAll(replacement)
        }

        private fun inArray(s: String, array: Array<String>?): Boolean {
            for (item in array!!) {
                if (item == s) {
                    return true
                }
            }
            return false
        }
    }
}