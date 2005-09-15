/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, iClick Inc.,
 * http://www.apache.org.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

/* Modified by Jeremy J. Carroll HP
 *
 * Was originally org/apache/xerces/utils/XercesURI.java in Xerces 1.4.4
 *
 *
 * Modifications are:
 * (c) Copyright 2001-2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.hp.hpl.jena.iri.impl;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.arp.MalformedURIException;
import com.hp.hpl.jena.rdf.arp.impl.CharacterModel;


/**********************************************************************
* A class to represent a Uniform Resource Identifier (XercesURI). This class
* is designed to handle the parsing of URIs and provide access to
* the various components (scheme, host, port, userinfo, path, query
* string and fragment) that may constitute a XercesURI.
* <p>
* Parsing of a XercesURI specification is done according to the XercesURI
* syntax described in RFC 2396
* <http://www.ietf.org/rfc/rfc2396.txt?number=2396>. Every XercesURI consists
* of a scheme, followed by a colon (':'), followed by a scheme-specific
* part. For URIs that follow the "generic XercesURI" syntax, the scheme-
* specific part begins with two slashes ("//") and may be followed
* by an authority segment (comprised of user information, host, and
* port), path segment, query segment and fragment. Note that RFC 2396
* no longer specifies the use of the parameters segment and excludes
* the "user:password" syntax as part of the authority segment. If
* "user:password" appears in a XercesURI, the entire user/password string
* is stored as userinfo.
* <p>
* For URIs that do not follow the "generic XercesURI" syntax (e.g. mailto),
* the entire scheme-specific part is treated as the "path" portion
* of the XercesURI.
* <p>
* Note that, unlike the java.net.URL class, this class does not provide
* any built-in network access functionality nor does it provide any
* scheme-specific functionality (for example, it does not know a
* default port for a specific scheme). Rather, it only knows the
* grammar and basic set of operations that can be applied to a XercesURI.
*
* @version  $Id: XercesURI.java,v 1.2 2005-09-15 14:31:19 jeremy_carroll Exp $
*
**********************************************************************/
public class XercesURI implements Serializable {
    
    /**
     * 
     */
    
    static Log logger = LogFactory.getLog(XercesURI.class);

    /** reserved characters */
    private static final String RESERVED_CHARACTERS = ";/?:@&=+$,[]";

    /** XercesURI punctuation mark characters - these, combined with
        alphanumerics, constitute the "unreserved" characters */
//  private static final String MARK_CHARACTERS = "-_.!~*'() ";

    /** scheme can be composed of alphanumerics and these characters */
    private static final String SCHEME_CHARACTERS = "+-.";

    /** userinfo can be composed of unreserved, escaped and these
        characters */
    private static final String USERINFO_CHARACTERS = ";:&=+$,";

    /** Stores the scheme (usually the protocol) for this XercesURI. */
    private String m_scheme = null;

    /** If specified, stores the userinfo for this XercesURI; otherwise null */
    private String m_userinfo = null;

    /** If specified, stores the host for this XercesURI; otherwise null */
    private String m_host = null;

    /** If specified, stores the port for this XercesURI; otherwise null */
    private String m_port = null;
    private int n_port = -1;

    /** If specified, stores the path for this XercesURI; otherwise null */
    private String m_path = null;
    /** Lazily assigned variable being: ??
     * { m_path +"a", dir, parent-dir, grand-parent-dir }
     * Each component is lazily assigned too.
     */
    private String m_subPaths[] = null;

    /** If specified, stores the query string for this XercesURI; otherwise
        null.  */
    private String m_queryString = null;

    /** If specified, stores the fragment for this XercesURI; otherwise null */
    private String m_fragment = null;

//  private static boolean DEBUG = false;

    /**
    * Construct a new and uninitialized XercesURI.
    */
    public XercesURI() {
    }

    /**
     * Construct a new XercesURI from another XercesURI. All fields for this XercesURI are
     * set equal to the fields of the XercesURI passed in.
     *
     * @param p_other the XercesURI to copy (cannot be null)
     */
    public XercesURI(XercesURI p_other) {
        initialize(p_other);
    }

    /**
     * Construct a new XercesURI from a XercesURI specification string. If the
     * specification follows the "generic XercesURI" syntax, (two slashes
     * following the first colon), the specification will be parsed
     * accordingly - setting the scheme, userinfo, host,port, path, query
     * string and fragment fields as necessary. If the specification does
     * not follow the "generic XercesURI" syntax, the specification is parsed
     * into a scheme and scheme-specific part (stored as the path) only.
     *
     * @param p_uriSpec the XercesURI specification string (cannot be null or
     *                  empty)
     *
     * @exception MalformedURIException if p_uriSpec violates any syntax
     *                                   rules
     */
    public XercesURI(String p_uriSpec) throws MalformedURIException {
        this((XercesURI) null, p_uriSpec);
    }

    /**
     * Construct a new XercesURI from a base XercesURI and a XercesURI specification string.
     * The XercesURI specification string may be a relative XercesURI.
     *
     * @param p_base the base XercesURI (cannot be null if p_uriSpec is null or
     *               empty)
     * @param p_uriSpec the XercesURI specification string (cannot be null or
     *                  empty if p_base is null)
     *
     * @exception MalformedURIException if p_uriSpec violates any syntax
     *                                  rules
     */
    public XercesURI(XercesURI p_base, String p_uriSpec) throws MalformedURIException {
        initialize(p_base, p_uriSpec);
    }

    /**
     * Construct a new XercesURI that does not follow the generic XercesURI syntax.
     * Only the scheme and scheme-specific part (stored as the path) are
     * initialized.
     *
     * @param p_scheme the XercesURI scheme (cannot be null or empty)
     * @param p_schemeSpecificPart the scheme-specific part (cannot be
     *                             null or empty)
     *
     * @exception MalformedURIException if p_scheme violates any
     *                                  syntax rules
     */
    public XercesURI(String p_scheme, String p_schemeSpecificPart)
        throws MalformedURIException {
        if (p_scheme == null || p_scheme.length() == 0) {
            throw new MalformedURIException("Cannot construct XercesURI with null/empty scheme!");
        }
        if (p_schemeSpecificPart == null
            || p_schemeSpecificPart.length() == 0) {
            throw new MalformedURIException("Cannot construct XercesURI with null/empty scheme-specific part!");
        }
        setScheme(p_scheme);
        setPath(p_schemeSpecificPart);
    }

    /**
     * Construct a new XercesURI that follows the generic XercesURI syntax from its
     * component parts. Each component is validated for syntax and some
     * basic semantic checks are performed as well.  See the individual
     * setter methods for specifics.
     *
     * @param p_scheme the XercesURI scheme (cannot be null or empty)
     * @param p_host the hostname or IPv4 address for the XercesURI
     * @param p_path the XercesURI path - if the path contains '?' or '#',
     *               then the query string and/or fragment will be
     *               set from the path; however, if the query and
     *               fragment are specified both in the path and as
     *               separate parameters, an exception is thrown
     * @param p_queryString the XercesURI query string (cannot be specified
     *                      if path is null)
     * @param p_fragment the XercesURI fragment (cannot be specified if path
     *                   is null)
     *
     * @exception MalformedURIException if any of the parameters violates
     *                                  syntax rules or semantic rules
     */
    public XercesURI(
        String p_scheme,
        String p_host,
        String p_path,
        String p_queryString,
        String p_fragment)
        throws MalformedURIException {
        this(p_scheme, null, p_host, -1, p_path, p_queryString, p_fragment);
    }

    /**
     * Construct a new XercesURI that follows the generic XercesURI syntax from its
     * component parts. Each component is validated for syntax and some
     * basic semantic checks are performed as well.  See the individual
     * setter methods for specifics.
     *
     * @param p_scheme the XercesURI scheme (cannot be null or empty)
     * @param p_userinfo the XercesURI userinfo (cannot be specified if host
     *                   is null)
     * @param p_host the hostname or IPv4 address for the XercesURI
     * @param p_port the XercesURI port (may be -1 for "unspecified"; cannot
     *               be specified if host is null)
     * @param p_path the XercesURI path - if the path contains '?' or '#',
     *               then the query string and/or fragment will be
     *               set from the path; however, if the query and
     *               fragment are specified both in the path and as
     *               separate parameters, an exception is thrown
     * @param p_queryString the XercesURI query string (cannot be specified
     *                      if path is null)
     * @param p_fragment the XercesURI fragment (cannot be specified if path
     *                   is null)
     *
     * @exception MalformedURIException if any of the parameters violates
     *                                  syntax rules or semantic rules
     */
    public XercesURI(
        String p_scheme,
        String p_userinfo,
        String p_host,
        int p_port,
        String p_path,
        String p_queryString,
        String p_fragment)
        throws MalformedURIException {
        if (p_scheme == null || p_scheme.length() == 0) {
            throw new MalformedURIException("Scheme is required!");
        }

        if (p_host == null) {
            if (p_userinfo != null) {
                throw new MalformedURIException("Userinfo may not be specified if host is not specified!");
            }
            if (p_port != -1) {
                throw new MalformedURIException("Port may not be specified if host is not specified!");
            }
        }

        if (p_path != null) {
            if (p_path.indexOf('?') != -1 && p_queryString != null) {
                throw new MalformedURIException("Query string cannot be specified in path and query string!");
            }

            if (p_path.indexOf('#') != -1 && p_fragment != null) {
                throw new MalformedURIException("Fragment cannot be specified in both the path and fragment!");
            }
        }

        setScheme(p_scheme);
        setHost(p_host);
        setPort(p_port);
        m_port = "" + p_port;
        setUserinfo(p_userinfo);
        setPath(p_path);
        setQueryString(p_queryString);
        setFragment(p_fragment);
    }

    /**
     * Initialize all fields of this XercesURI from another XercesURI.
     *
     * @param p_other the XercesURI to copy (cannot be null)
     */
    private void initialize(XercesURI p_other) {
        m_scheme = p_other.getScheme();
        m_userinfo = p_other.getUserinfo();
        m_host = p_other.getHost();
        m_port = p_other.m_port;
        n_port = p_other.n_port;
        m_path = p_other.getPath();
        m_queryString = p_other.getQueryString();
        m_fragment = p_other.getFragment();
    }

    /**
     * Initializes this XercesURI from a base XercesURI and a XercesURI specification string.
     * See RFC 2396 Section 4 and Appendix B for specifications on parsing
     * the XercesURI and Section 5 for specifications on resolving relative URIs
     * and relative paths.
     *
     * @param p_base the base XercesURI (may be null if p_uriSpec is an absolute
     *               XercesURI)
     * @param p_uriSpec the XercesURI spec string which may be an absolute or
     *                  relative XercesURI (can only be null/empty if p_base
     *                  is not null)
     *
     * @exception MalformedURIException if p_base is null and p_uriSpec
     *                                  is not an absolute XercesURI or if
     *                                  p_uriSpec violates syntax rules
     */
    private void initialize(XercesURI p_base, String p_uriSpec)
        throws MalformedURIException {
        if (p_base == null && (p_uriSpec == null || p_uriSpec.length() == 0)) {
            throw new MalformedURIException("Cannot initialize XercesURI with empty parameters.");
        }

        // just make a copy of the base if spec is empty
        if (p_uriSpec == null || p_uriSpec.length() == 0) {
            initialize(p_base);
            return;
        }

        String uriSpec = p_uriSpec;
        int uriSpecLen = uriSpec.length();
        int index = 0;

        // Check for scheme, which must be before '/', '?' or '#'. Also handle
        // names with DOS drive letters ('D:'), so 1-character schemes are not
        // allowed.
        int colonIdx = uriSpec.indexOf(':');
        int slashIdx = uriSpec.indexOf('/');
        int queryIdx = uriSpec.indexOf('?');
        int fragmentIdx = uriSpec.indexOf('#');

        if ((colonIdx < 2)
            || (colonIdx > slashIdx && slashIdx != -1)
            || (colonIdx > queryIdx && queryIdx != -1)
            || (colonIdx > fragmentIdx && fragmentIdx != -1)) {
            // We need to do the relative XercesURI algorithm:

            // jjc: the spec says:
            // 'XercesURI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]'
            // My understanding is that if there is only the fragment
            // then this is a relative XercesURI.
            if (p_base == null // del jjc: && fragmentIdx != 0
            ) {
                // Nothing to be relative against.
                throw new MalformedURIException("No scheme found in XercesURI '" + p_uriSpec + "'" );
            } 
            if ((!p_base.isGenericURI()) && fragmentIdx != 0)
                    // Can't be relative against opaque XercesURI (except using the #frag).
                    throw new MalformedURIException("Cannot apply relative XercesURI to an opaque XercesURI");
            
        } else {
            initializeScheme(uriSpec);
            index = m_scheme.length() + 1;
        }

        // two slashes means generic XercesURI syntax, so we get the authority
        if (((index + 1) < uriSpecLen)
            && (uriSpec.substring(index).startsWith("//"))) {
            index += 2;
            int startPos = index;

            // get authority - everything up to path, query or fragment
            char testChar = '\0';
            while (index < uriSpecLen) {
                testChar = uriSpec.charAt(index);
                if (testChar == '/' || testChar == '?' || testChar == '#') {
                    break;
                }
                index++;
            }

            // if we found authority, parse it out, otherwise we set the
            // host to empty string
            if (index > startPos) {
                initializeAuthority(uriSpec.substring(startPos, index));
            } else {
                m_host = "";
            }
        }

        initializePath(uriSpec.substring(index));

        // Resolve relative XercesURI to base XercesURI - see RFC 2396 Section 5.2
        // In some cases, it might make more sense to throw an exception
        // (when scheme is specified is the string spec and the base XercesURI
        // is also specified, for example), but we're just following the
        // RFC specifications
        if (p_base != null) {

            // check to see if this is the current doc - RFC 2396 5.2 #2
            // note that this is slightly different from the RFC spec in that
            // we don't include the check for query string being null
            // - this handles cases where the urispec is just a query
            // string or a fragment (e.g. "?y" or "#s") -
            // see <http://www.ics.uci.edu/~fielding/url/test1.html> which
            // identified this as a bug in the RFC
            if (m_path.length() == 0 && m_scheme == null && m_host == null) {
                m_scheme = p_base.getScheme();
                m_userinfo = p_base.getUserinfo();
                m_host = p_base.getHost();
                m_port = p_base.m_port;
                n_port = p_base.getPort();
                m_path = p_base.getPath();

                if (m_queryString == null) {
                    m_queryString = p_base.getQueryString();
                }
                return;
            }

            // check for scheme - RFC 2396 5.2 #3
            // if we found a scheme, it means absolute XercesURI, so we're done
            if (m_scheme == null) {
                m_scheme = p_base.getScheme();
            } else {
                return;
            }

            // check for authority - RFC 2396 5.2 #4
            // if we found a host, then we've got a network path, so we're done
            if (m_host == null) {
                m_userinfo = p_base.getUserinfo();
                m_host = p_base.getHost();
                m_port = p_base.m_port;
                n_port = p_base.getPort();
            } else {
                return;
            }

            // check for absolute path - RFC 2396 5.2 #5
            if (m_path.length() > 0 && m_path.startsWith("/")) {
                return;
            }

            // if we get to this point, we need to resolve relative path
            // RFC 2396 5.2 #6
                String path = // jjc new String();
    "/"; // jjc ins
            String basePath = p_base.getPath();

            // 6a - get all but the last segment of the base XercesURI path
            if (basePath != null) {
                int lastSlash = basePath.lastIndexOf('/');
                if (lastSlash != -1) {
                    path = basePath.substring(0, lastSlash + 1);
                }
            }

            // 6b - append the relative XercesURI path
            path = path.concat(m_path);

            // 6c - remove all "./" where "." is a complete path segment
            index = -1;
            while ((index = path.indexOf("/./")) != -1) {
                path =
                    path.substring(0, index + 1).concat(
                        path.substring(index + 3));
            }

            // 6d - remove "." if path ends with "." as a complete path segment
            if (path.endsWith("/.")) {
                path = path.substring(0, path.length() - 1);
            }

            // 6e - remove all "<segment>/../" where "<segment>" is a complete
            // path segment not equal to ".."
            index = 1;
            int segIndex = -1;

            while ((index = path.indexOf("/../", index)) > 0) {
                segIndex = path.lastIndexOf('/', index - 1);
                if (segIndex != -1
                    && !path.substring(segIndex + 1, index).equals("..")) {
                    path =
                        path.substring(0, segIndex).concat(
                            path.substring(index + 3));
                    index = segIndex;
                } else {
                    index += 4;
                }
            }

            // 6f - remove ending "<segment>/.." where "<segment>" is a
            // complete path segment
            if (path.endsWith("/..")) {
                index = path.length() - 3;
                segIndex = path.lastIndexOf('/', index - 1);
                if (segIndex != -1
                    && !path.substring(segIndex + 1, index).equals("..")) {
                    path = path.substring(0, segIndex + 1);
                }
            }

            m_path = path;
        }
    }

    /**
     * Initialize the scheme for this XercesURI from a XercesURI string spec.
     *
     * @param p_uriSpec the XercesURI specification (cannot be null)
     *
     * @exception MalformedURIException if XercesURI does not have a conformant
     *                                  scheme
     */
    private void initializeScheme(String p_uriSpec)
        throws MalformedURIException {
        int uriSpecLen = p_uriSpec.length();
        int index = p_uriSpec.indexOf(':');

        if (index < 1)
            throw new MalformedURIException("No scheme found in XercesURI '" + p_uriSpec + "'" );

        if (index == uriSpecLen - 1)
            throw new MalformedURIException( "A bare scheme name is not a XercesURI: '" +  p_uriSpec + "'" );

        setScheme(p_uriSpec.substring(0, index));
    }

    /**
     * Initialize the authority (userinfo, host and port) for this
     * XercesURI from a XercesURI string spec.
     *
     * @param p_uriSpec the XercesURI specification (cannot be null)
     *
     * @exception MalformedURIException if p_uriSpec violates syntax rules
     */
    private void initializeAuthority(String p_uriSpec)
        throws MalformedURIException {
        int index = 0;
        int start = 0;
        int end = p_uriSpec.length();
        char testChar = '\0';
        String userinfo = null;

        // userinfo is everything up @
        if (p_uriSpec.indexOf('@', start) != -1) {
            while (index < end) {
                testChar = p_uriSpec.charAt(index);
                if (testChar == '@') {
                    break;
                }
                index++;
            }
            userinfo = p_uriSpec.substring(start, index);
            index++;
        }

        // host is everything up to ':'
        String host = null;
        start = index;
        while (index < end) {
            testChar = p_uriSpec.charAt(index);
            if (testChar == ':') {
                break;
            }
            index++;
        }
        host = p_uriSpec.substring(start, index);
        int port = -1;
        if (host.length() > 0) {
            // port
            if (testChar == ':') {
                index++;
                start = index;
                while (index < end) {
                    index++;
                }
                String portStr = p_uriSpec.substring(start, index);
                if (portStr.length() > 0) {
                    for (int i = 0; i < portStr.length(); i++) {
                        if (!isDigit(portStr.charAt(i))) {
                            throw new MalformedURIException(
                                portStr
                                    + " is invalid. Port should only contain digits!");
                        }
                    }
                    try {
                        port = Integer.parseInt(portStr);
                        m_port = portStr;
                    } catch (NumberFormatException nfe) {
                        // can't happen
                    }
                }
            }
        }
        setHost(host);
        setPort(port);
        setUserinfo(userinfo);
    }

    /**
     * Initialize the path for this XercesURI from a XercesURI string spec.
     *
     * @param p_uriSpec the XercesURI specification (cannot be null)
     *
     * @exception MalformedURIException if p_uriSpec violates syntax rules
     */
    private void initializePath(String p_uriSpec)
        throws MalformedURIException {
        if (p_uriSpec == null) {
            throw new MalformedURIException("Cannot initialize path from null string!");
        }

        int index = 0;
        int start = 0;
        int end = p_uriSpec.length();
        char testChar = '\0';

        // path - everything up to query string or fragment
        while (index < end) {
            testChar = p_uriSpec.charAt(index);
            if (testChar == '?' || testChar == '#') {
                break;
            }
            // check for valid escape sequence
            if (testChar == '%') {
                if (index + 2 >= end
                    || !isHex(p_uriSpec.charAt(index + 1))
                    || !isHex(p_uriSpec.charAt(index + 2))) {
                    throw new MalformedURIException( "Path contains invalid escape sequence: " + p_uriSpec );
                }
            } else if (
                !isReservedCharacter(testChar)
                    && !isUnreservedCharacter(testChar)) {
                throw new MalformedURIException(
                    "Path '" + p_uriSpec + "' contains invalid character: " + testChar);
            }
            index++;
        }
        m_path = p_uriSpec.substring(start, index);

        // query - starts with ? and up to fragment or end
        if (testChar == '?') {
            index++;
            start = index;
            while (index < end) {
                testChar = p_uriSpec.charAt(index);
                if (testChar == '#') {
                    break;
                }
                if (testChar == '%') {
                    if (index + 2 >= end
                        || !isHex(p_uriSpec.charAt(index + 1))
                        || !isHex(p_uriSpec.charAt(index + 2))) {
                        throw new MalformedURIException("Query string contains invalid escape sequence in '" + p_uriSpec + "'" );
                    }
                } else if (
                    !isReservedCharacter(testChar)
                        && !isUnreservedCharacter(testChar)) {
                    throw new MalformedURIException( "Query string contains invalid character '" + testChar + "' in '" + p_uriSpec + "'" );
                }
                index++;
            }
            m_queryString = p_uriSpec.substring(start, index);
        }

        // fragment - starts with #
        if (testChar == '#') {
            index++;
            start = index;
            while (index < end) {
                testChar = p_uriSpec.charAt(index);

                if (testChar == '%') {
                    if (index + 2 >= end
                        || !isHex(p_uriSpec.charAt(index + 1))
                        || !isHex(p_uriSpec.charAt(index + 2))) {
                        throw new MalformedURIException( "Fragment contains invalid escape sequence in '" + p_uriSpec + "'" );
                    }
                } else if (
                    !isReservedCharacter(testChar)
                        && !isUnreservedCharacter(testChar)) {
                    throw new MalformedURIException(
                        "Fragment contains invalid character '" + testChar + "' in '" + p_uriSpec + "'" );
                }
                index++;
            }
            m_fragment = p_uriSpec.substring(start, index);
        }
    }

    /**
     * Get the scheme for this XercesURI.
     *
     * @return the scheme for this XercesURI
     */
    public String getScheme() {
        return m_scheme;
    }

    /**
     * Get the scheme-specific part for this XercesURI (everything following the
     * scheme and the first colon). See RFC 2396 Section 5.2 for spec.
     *
     * @return the scheme-specific part for this XercesURI
     */
    public String getSchemeSpecificPart() {
        StringBuffer schemespec = new StringBuffer();

        if (m_userinfo != null || m_host != null || m_port != null) {
            schemespec.append("//");
        }

        if (m_userinfo != null) {
            schemespec.append(m_userinfo);
            schemespec.append('@');
        }

        if (m_host != null) {
            schemespec.append(m_host);
        }

        if (m_port != null) {
            schemespec.append(':');
            schemespec.append(m_port);
        }

        if (m_path != null) {
            schemespec.append((m_path));
        }

        if (m_queryString != null) {
            schemespec.append('?');
            schemespec.append(m_queryString);
        }

        if (m_fragment != null) {
            schemespec.append('#');
            schemespec.append(m_fragment);
        }

        return schemespec.toString();
    }

    /**
     * Get the userinfo for this XercesURI.
     *
     * @return the userinfo for this XercesURI (null if not specified).
     */
    public String getUserinfo() {
        return m_userinfo;
    }

    /**
    * Get the host for this XercesURI.
    *
    * @return the host for this XercesURI (null if not specified).
    */
    public String getHost() {
        return m_host;
    }

    /**
     * Get the port for this XercesURI.
     *
     * @return the port for this XercesURI (-1 if not specified).
     */
    public int getPort() {
        return n_port;
    }

    /**
     * Get the path for this XercesURI (optionally with the query string and
     * fragment).
     *
     * @param p_includeQueryString if true (and query string is not null),
     *                             then a "?" followed by the query string
     *                             will be appended
     * @param p_includeFragment if true (and fragment is not null),
     *                             then a "#" followed by the fragment
     *                             will be appended
     *
     * @return the path for this XercesURI possibly including the query string
     *         and fragment
     */
    public String getPath(
        boolean p_includeQueryString,
        boolean p_includeFragment) {
        StringBuffer pathString = new StringBuffer(m_path);

        if (p_includeQueryString && m_queryString != null) {
            pathString.append('?');
            pathString.append(m_queryString);
        }

        if (p_includeFragment && m_fragment != null) {
            pathString.append('#');
            pathString.append(m_fragment);
        }
        return pathString.toString();
    }

    /**
     * Get the path for this XercesURI. Note that the value returned is the path
     * only and does not include the query string or fragment.
     *
     * @return the path for this XercesURI.
     */
    public String getPath() {
        return m_path;
    }

    /**
     * Get the query string for this XercesURI.
     *
     * @return the query string for this XercesURI. Null is returned if there
     *         was no "?" in the XercesURI spec, empty string if there was a
     *         "?" but no query string following it.
     */
    public String getQueryString() {
        return m_queryString;
    }

    /**
     * Get the fragment for this XercesURI.
     *
     * @return the fragment for this XercesURI. Null is returned if there
     *         was no "#" in the XercesURI spec, empty string if there was a
     *         "#" but no fragment following it.
     */
    public String getFragment() {
        return m_fragment;
    }

    /**
     * Set the scheme for this XercesURI. The scheme is converted to lowercase
     * before it is set.
     *
     * @param p_scheme the scheme for this XercesURI (cannot be null)
     *
     * @exception MalformedURIException if p_scheme is not a conformant
     *                                  scheme name
     */
    private void setScheme(String p_scheme) throws MalformedURIException {
        if (p_scheme == null) {
            throw new MalformedURIException("Cannot set scheme from null string!");
        }
        if (!isConformantSchemeName(p_scheme)) {
            throw new MalformedURIException("The scheme '" + p_scheme + "' is not conformant.");
        }

        m_scheme = p_scheme; //.toLowerCase();
    }

    /**
     * Set the userinfo for this XercesURI. If a non-null value is passed in and
     * the host value is null, then an exception is thrown.
     *
     * @param p_userinfo the userinfo for this XercesURI
     *
     * @exception MalformedURIException if p_userinfo contains invalid
     *                                  characters
     */
    private void setUserinfo(String p_userinfo) throws MalformedURIException {
        if (p_userinfo == null) {
            m_userinfo = null;
        } else {
            if (m_host == null) {
                throw new MalformedURIException("Userinfo cannot be set when host is null!");
            }

            // userinfo can contain alphanumerics, mark characters, escaped
            // and ';',':','&','=','+','$',','
            int index = 0;
            int end = p_userinfo.length();
            char testChar = '\0';
            while (index < end) {
                testChar = p_userinfo.charAt(index);
                if (testChar == '%') {
                    if (index + 2 >= end
                        || !isHex(p_userinfo.charAt(index + 1))
                        || !isHex(p_userinfo.charAt(index + 2))) {
                        throw new MalformedURIException("Userinfo contains invalid escape sequence!");
                    }
                } else if (
                    !isUnreservedCharacter(testChar)
                        && USERINFO_CHARACTERS.indexOf(testChar) == -1) {
                    throw new MalformedURIException(
                        "Userinfo contains invalid character:" + testChar);
                }
                index++;
            }
        }
        m_userinfo = p_userinfo;
    }

    /**
    * Set the host for this XercesURI. If null is passed in, the userinfo
    * field is also set to null and the port is set to -1.
    *
    * @param p_host the host for this XercesURI
    *
    * @exception MalformedURIException if p_host is not a valid IP
    *                                  address or DNS hostname.
    */
    private void setHost(String p_host) throws MalformedURIException {
        if (p_host == null || p_host.length() == 0) {
            m_host = p_host;
            m_userinfo = null;
            m_port = null;
            n_port = -1;
        } else if (!isWellFormedAddress(p_host)) {
            throw new MalformedURIException( "Host is not a well formed address in '" + p_host + "'" );
        }
        m_host = p_host;
    }

    /**
     * Set the port for this XercesURI. -1 is used to indicate that the port is
     * not specified, otherwise valid port numbers are  between 0 and 65535.
     * If a valid port number is passed in and the host field is null,
     * an exception is thrown.
     *
     * @param p_port the port number for this XercesURI
     *
     * @exception MalformedURIException if p_port is not -1 and not a
     *                                  valid port number
     */
    private void setPort(int p_port) throws MalformedURIException {
        if (p_port >= 0 && p_port <= 65535) {
            if (m_host == null) {
                throw new MalformedURIException("Port cannot be set when host is null!");
            }
        } else if (p_port != -1) {
            throw new MalformedURIException("Invalid port number!");
        }
        n_port = p_port;

    }

    /**
     * Set the path for this XercesURI. If the supplied path is null, then the
     * query string and fragment are set to null as well. If the supplied
     * path includes a query string and/or fragment, these fields will be
     * parsed and set as well. Note that, for URIs following the "generic
     * XercesURI" syntax, the path specified should start with a slash.
     * For URIs that do not follow the generic XercesURI syntax, this method
     * sets the scheme-specific part.
     *
     * @param p_path the path for this XercesURI (may be null)
     *
     * @exception MalformedURIException if p_path contains invalid
     *                                  characters
     */
    private void setPath(String p_path) throws MalformedURIException {
        if (p_path == null) {
            m_path = null;
            m_queryString = null;
            m_fragment = null;
        } else {
            initializePath(p_path);
        }
    }

    /**
     * Append to the end of the path of this XercesURI. If the current path does
     * not end in a slash and the path to be appended does not begin with
     * a slash, a slash will be appended to the current path before the
     * new segment is added. Also, if the current path ends in a slash
     * and the new segment begins with a slash, the extra slash will be
     * removed before the new segment is appended.
     *
     * @param p_addToPath the new segment to be added to the current path
     *
     * @exception MalformedURIException if p_addToPath contains syntax
     *                                  errors
     */
//  private void appendPath(String p_addToPath) throws MalformedURIException {
//      if (p_addToPath == null || p_addToPath.length() == 0) {
//          return;
//      }
//
//      if (!isURIString(p_addToPath)) {
//          throw new MalformedURIException("Path contains invalid character!");
//      }
//
//      if (m_path == null || m_path.length() == 0) {
//          if (p_addToPath.startsWith("/")) {
//              m_path = p_addToPath;
//          } else {
//              m_path = "/" + p_addToPath;
//          }
//      } else if (m_path.endsWith("/")) {
//          if (p_addToPath.startsWith("/")) {
//              m_path = m_path.concat(p_addToPath.substring(1));
//          } else {
//              m_path = m_path.concat(p_addToPath);
//          }
//      } else {
//          if (p_addToPath.startsWith("/")) {
//              m_path = m_path.concat(p_addToPath);
//          } else {
//              m_path = m_path.concat("/" + p_addToPath);
//          }
//      }
//  }

    /**
     * Set the query string for this XercesURI. A non-null value is valid only
     * if this is an XercesURI conforming to the generic XercesURI syntax and
     * the path value is not null.
     *
     * @param p_queryString the query string for this XercesURI
     *
     * @exception MalformedURIException if p_queryString is not null and this
     *                                  XercesURI does not conform to the generic
     *                                  XercesURI syntax or if the path is null
     */
    private void setQueryString(String p_queryString)
        throws MalformedURIException {
        if (p_queryString == null) {
            m_queryString = null;
        } else if (!isGenericURI()) {
            throw new MalformedURIException("Query string can only be set for a generic XercesURI!");
        } else if (getPath() == null) {
            throw new MalformedURIException("Query string cannot be set when path is null!");
        } else if (!isURIString(p_queryString)) {
            throw new MalformedURIException("Query string contains invalid character!");
        } else {
            m_queryString = p_queryString;
        }
    }

    /**
     * Set the fragment for this XercesURI. A non-null value is valid only
     * if this is a XercesURI conforming to the generic XercesURI syntax and
     * the path value is not null.
     *
     * @param p_fragment the fragment for this XercesURI
     *
     * @exception MalformedURIException if p_fragment is not null and this
     *                                  XercesURI does not conform to the generic
     *                                  XercesURI syntax or if the path is null
     */
    public void setFragment(String p_fragment) throws MalformedURIException {
        if (p_fragment == null) {
            m_fragment = null;
        } else if (!isGenericURI()) {
            throw new MalformedURIException("Fragment can only be set for a generic XercesURI!");
        } else if (getPath() == null) {
            throw new MalformedURIException("Fragment cannot be set when path is null!");
        } else if (!isURIString(p_fragment)) {
            throw new MalformedURIException("Fragment contains invalid character!");
        } else {
            m_fragment = p_fragment;
        }
    }

    /**
     * Determines if the passed-in Object is equivalent to this XercesURI.
     *
     * @param p_test the Object to test for equality.
     *
     * @return true if p_test is a XercesURI with all values equal to this
     *         XercesURI, false otherwise
     */
    public boolean equals(Object p_test) {
        if (p_test instanceof XercesURI) {
            XercesURI testURI = (XercesURI) p_test;
            if (((m_scheme == null && testURI.m_scheme == null)
                || (m_scheme != null
                    && testURI.m_scheme != null
                    && m_scheme.equals(testURI.m_scheme)))
                && ((m_userinfo == null && testURI.m_userinfo == null)
                    || (m_userinfo != null
                        && testURI.m_userinfo != null
                        && m_userinfo.equals(testURI.m_userinfo)))
                && ((m_host == null && testURI.m_host == null)
                    || (m_host != null
                        && testURI.m_host != null
                        && m_host.equals(testURI.m_host)))
                && ((m_port == null && testURI.m_port == null)
                    || (m_port != null
                        && testURI.m_port != null
                        && m_port.equals(testURI.m_port)))
                && ((m_path == null && testURI.m_path == null)
                    || (m_path != null
                        && testURI.m_path != null
                        && m_path.equals(testURI.m_path)))
                && ((m_queryString == null && testURI.m_queryString == null)
                    || (m_queryString != null
                        && testURI.m_queryString != null
                        && m_queryString.equals(testURI.m_queryString)))
                && ((m_fragment == null && testURI.m_fragment == null)
                    || (m_fragment != null
                        && testURI.m_fragment != null
                        && m_fragment.equals(testURI.m_fragment)))) {
                return true;
            }
        }
        return false;
    }


    /**
        produce a human-consumable string for the XercesURI
    */
    public String toString() 
        { return getURIString(); }
    
    /**
     * Get the XercesURI as a string specification. See RFC 2396 Section 5.2.
     *
     * @return the XercesURI string specification
     */
    public String getURIString() {
        StringBuffer uriSpecString = new StringBuffer();

        if (m_scheme != null) {
            uriSpecString.append(m_scheme);
            uriSpecString.append(':');
        }
        uriSpecString.append(getSchemeSpecificPart());
        return uriSpecString.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }
    
    /**
     * Get the indicator as to whether this XercesURI uses the "generic XercesURI"
     * syntax.
     *
     * @return true if this XercesURI uses the "generic XercesURI" syntax, false
     *         otherwise
     */
    public boolean isGenericURI() {
        // presence of the host (whether valid or empty) means
        // double-slashes which means generic uri
        return (m_host != null);
    }

    /**
     * Determine whether a scheme conforms to the rules for a scheme name.
     * A scheme is conformant if it starts with an alphanumeric, and
     * contains only alphanumerics, '+','-' and '.'.
     *
     * @return true if the scheme is conformant, false otherwise
     */
    public static boolean isConformantSchemeName(String p_scheme) {
        if (p_scheme == null || p_scheme.length() == 0) {
            return false;
        }

        if (!isAlpha(p_scheme.charAt(0))) {
            return false;
        }

        char testChar;
        for (int i = 1; i < p_scheme.length(); i++) {
            testChar = p_scheme.charAt(i);
            if (!isAlphanum(testChar)
                && SCHEME_CHARACTERS.indexOf(testChar) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine whether a string is syntactically capable of representing
     * a valid IPv4 address or the domain name of a network host. A valid
     * IPv4 address consists of four decimal digit groups separated by a
     * '.'. A hostname consists of domain labels (each of which must
     * begin and end with an alphanumeric but may contain '-') separated
     & by a '.'. See RFC 2396 Section 3.2.2.
     *
     * @return true if the string is a syntactically valid IPv4 address
     *              or hostname
     */
    public static boolean isWellFormedAddress(String p_address) {
        if (p_address == null) {
            return false;
        }

        String address = p_address;
        int addrLength = address.length();
        if (addrLength == 0 || addrLength > 255) {
            return false;
        }

        if (address.startsWith(".") || address.startsWith("-")) {
            return false;
        }

        // rightmost domain label starting with digit indicates IP address
        // since top level domain label can only start with an alpha
        // see RFC 2396 Section 3.2.2
        int index = address.lastIndexOf('.');
        if (address.endsWith(".")) {
            index = address.substring(0, index).lastIndexOf('.');
        }

        if (index + 1 < addrLength && isDigit(p_address.charAt(index + 1))) {
            char testChar;
            int numDots = 0;

            // make sure that 1) we see only digits and dot separators, 2) that
            // any dot separator is preceded and followed by a digit and
            // 3) that we find 3 dots
            for (int i = 0; i < addrLength; i++) {
                testChar = address.charAt(i);
                if (testChar == '.') {
                    if (!isDigit(address.charAt(i - 1))
                        || (i + 1 < addrLength
                            && !isDigit(address.charAt(i + 1)))) {
                        return false;
                    }
                    numDots++;
                } else if (!isDigit(testChar)) {
                    return false;
                }
            }
            if (numDots != 3) {
                return false;
            }
        } else {
            // domain labels can contain alphanumerics and '-"
            // but must start and end with an alphanumeric
            char testChar;

            for (int i = 0; i < addrLength; i++) {
                testChar = address.charAt(i);
                if (testChar == '.') {
                    if (!isAlphanum(address.charAt(i - 1))) {
                        return false;
                    }
                    if (i + 1 < addrLength
                        && !isAlphanum(address.charAt(i + 1))) {
                        return false;
                    }
                } else if (!isAlphanum(testChar) && testChar != '-') {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine whether a char is a digit.
     *
     * @return true if the char is betweeen '0' and '9', false otherwise
     */
    private static boolean isDigit(char p_char) {
        return p_char >= '0' && p_char <= '9';
    }

    /**
     * Determine whether a character is a hexadecimal character.
     *
     * @return true if the char is betweeen '0' and '9', 'a' and 'f'
     *         or 'A' and 'F', false otherwise
     */
    private static boolean isHex(char p_char) {
        return (
            isDigit(p_char)
                || (p_char >= 'a' && p_char <= 'f')
                || (p_char >= 'A' && p_char <= 'F'));
    }

    /**
     * Determine whether a char is an alphabetic character: a-z or A-Z
     *
     * @return true if the char is alphabetic, false otherwise
     */
    private static boolean isAlpha(char p_char) {
        return (
            (p_char >= 'a' && p_char <= 'z')
                || (p_char >= 'A' && p_char <= 'Z'));
    }

    /**
     * Determine whether a char is an alphanumeric: 0-9, a-z or A-Z
     *
     * @return true if the char is alphanumeric, false otherwise
     */
    private static boolean isAlphanum(char p_char) {
        return (isAlpha(p_char) || isDigit(p_char));
    }

    /**
     * Determine whether a character is a reserved character:
     * ';', '/', '?', ':', '@', '&', '=', '+', '$' or ','
     *
     * @return true if the string contains any reserved characters
     */
    private static boolean isReservedCharacter(char p_char) {
        return RESERVED_CHARACTERS.indexOf(p_char) != -1;
    }

    /**
     * Determine whether a char is an unreserved character.
     *
     * @return true if the char is unreserved, false otherwise
     */
    private static boolean isUnreservedCharacter(char p_char) {
        return (!isReservedCharacter(p_char)) && "#%[]".indexOf(p_char) == -1;
        //   return (isAlphanum(p_char) ||
        //           MARK_CHARACTERS.indexOf(p_char) != -1);
    }

    private boolean haveCheckedNFC = false;
    private boolean isNFC;

    public boolean isNormalFormC() {
        if (!haveCheckedNFC) {
            isNFC = CharacterModel.isNormalFormC(toString());
            haveCheckedNFC = true;
        }
        return isNFC;
    }
    /**
     * Determine whether a given string contains only XercesURI characters (also
     * called "uric" in RFC 2396). uric consist of all reserved
     * characters, unreserved characters and escaped characters.
     *
     * @return true if the string is comprised of uric, false otherwise
     */
    private static boolean isURIString(String p_uric) {
        if (p_uric == null) {
            return false;
        }
        int end = p_uric.length();
        char testChar = '\0';
        for (int i = 0; i < end; i++) {
            testChar = p_uric.charAt(i);
            if (testChar == '%') {
                if (i + 2 >= end
                    || !isHex(p_uric.charAt(i + 1))
                    || !isHex(p_uric.charAt(i + 2))) {
                    return false;
                } 
                    i += 2;
                    continue;
                
            }
            if (isReservedCharacter(testChar)
                || isUnreservedCharacter(testChar)) {
                continue;
            } 
                return false;
            
        }
        return true;
    }

    /* RELATIVIZE */

    static final public int SAMEDOCUMENT = 1;
    static final public int NETWORK = 2;
    static final public int ABSOLUTE = 4;
    static final public int RELATIVE = 8;
    static final public int PARENT = 16;
    static final public int GRANDPARENT = 32;

    private boolean equal(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }
    static private int prefs[][] = {
        { RELATIVE, RELATIVE|PARENT|GRANDPARENT },
        { PARENT, PARENT|GRANDPARENT },
        { GRANDPARENT, GRANDPARENT }
    };
    static String exact[] = { ".", "..", "../.." };
    static String sub[] = { "", "../", "../../" };
    public String relativize(String abs, int flags) throws MalformedURIException {
        XercesURI r;
            r = new XercesURI(abs);
       // logger.info("<"+Util.substituteStandardEntities(abs)+">");
       // logger.info("<"+Util.substituteStandardEntities(r.m_path)+">");
        if (r.isGenericURI()) {
            boolean net = equal(r.m_scheme, m_scheme);
            boolean absl =
                net
                    && equal(r.m_host, m_host)
                    && equal(m_userinfo, r.m_userinfo)
                    && equal(m_port, r.m_port);
            boolean same =
                absl
                    && equal(m_path, r.m_path)
                    && equal(m_queryString, r.m_queryString);

            String rslt = r.m_fragment == null ? "" : ("#" + r.m_fragment);

            if (same && (flags & SAMEDOCUMENT) != 0)
                return rslt;
            if (r.m_queryString != null) {
                rslt = "?" + r.m_queryString + rslt;
            }
            if ( absl ) {
                if ( m_subPaths == null ) {
                    m_subPaths = new String[]{ 
                        m_path==null?null:(m_path + "a"),
                         null, null, null };
                }
                if ( m_subPaths[0] != null )
                for (int i=0; i<3; i++) {
                    if ( (flags & prefs[i][1]) == 0 )
                        break;
                    if ( m_subPaths[i+1] == null )
                       m_subPaths[i+1] = getLastSlash(m_subPaths[i]);
                    if ( m_subPaths[i+1].length() == 0 )
                       break;
                    if ( (flags & prefs[i][0]) == 0 )
                       continue;
                    if ( !r.m_path.startsWith(m_subPaths[i+1]) )
                       continue;
                    // A relative path can be constructed.
                    int lg = m_subPaths[i+1].length();
                    if (lg == r.m_path.length()) {
                        return exact[i] + rslt;
                    }
                    rslt = sub[i] + r.m_path.substring(lg) + rslt;

              //      logger.info("<"+Util.substituteStandardEntities(rslt)+">["+i+"]");
                    return rslt;
                }
            }
            rslt = r.m_path + rslt;
            if ( absl && (flags & ABSOLUTE ) != 0 ) {
                return rslt;
            }
            if ( net && ( flags & NETWORK ) != 0 ) {
                return "//" + 
                ( r.m_userinfo == null ? "" : ( r.m_userinfo + "@") ) +
                r.m_host +
                ( r.m_port == null ? "" : ( ":" + r.m_port) ) +
                rslt;
                
            }
        }
        return abs;
    }
    static private String getLastSlash(String s) {
        int ix = s.lastIndexOf('/',s.length()-2);
        return s.substring(0,ix+1);
    }
    
}
