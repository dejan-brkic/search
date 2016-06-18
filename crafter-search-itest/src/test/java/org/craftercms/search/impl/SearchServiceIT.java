/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.search.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.craftercms.search.service.impl.SolrQuery;
import org.craftercms.search.service.impl.SolrRestClientSearchService;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test of the search service client/server.
 *
 * @author Alfonso Vásquez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/application-context.xml")
public class SearchServiceIT {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceIT.class);

    private static final String DEFAULT_SITE = "default";
    private static final String PLUTON_SITE = "pluton";
    private static final String PLUTON_INDEX_ID = "pluton";
    private static final String IPAD_DOC_ID = "ipad.xml";
    private static final String WP_REASONS_PDF_DOC_ID = "crafter-wp-7-reasons.pdf";

    private static final List<String> WP_REASONS_PDF_TAGS = Arrays.asList("Crafter", "reasons", "white paper");

    @Autowired
    private SolrRestClientSearchService searchService;

    @Test
    public void testMethodsWithNoIndexId() throws Exception {
        SolrQuery query = searchService.createQuery();
        query.setQuery("*:*");

        Map<String, Object> results = searchService.search(query);
        assertNotNull(results);

        Map<String, Object> response = getQueryResponse(results);
        assertEquals(0, getNumDocs(response));

        String xml = getClasspathFileContent("docs/" + IPAD_DOC_ID);
        String updateResponse = searchService.update(DEFAULT_SITE, IPAD_DOC_ID, xml, true);
        logger.info(updateResponse);

        File file = getClasspathFile("docs/" + WP_REASONS_PDF_DOC_ID);
        updateResponse = searchService.updateFile(DEFAULT_SITE, WP_REASONS_PDF_DOC_ID, file);
        logger.info(updateResponse);

        String commitResponse = searchService.commit();
        logger.info(commitResponse);

        results = searchService.search(query);
        assertNotNull(results);

        response = getQueryResponse(results);
        assertEquals(2, getNumDocs(response));

        Map<String, Map<String, Object>> docs = getDocs(response);
        Map<String, Object> ipadDoc = docs.get(IPAD_DOC_ID);
        Map<String, Object> wpReasonsPdfDoc = docs.get(WP_REASONS_PDF_DOC_ID);

        assertNotNull(ipadDoc);
        assertNotNull(wpReasonsPdfDoc);
        assertIPadDoc(ipadDoc, DEFAULT_SITE);
        assertWpReasonsPdfDoc(wpReasonsPdfDoc, DEFAULT_SITE);

        MultiValueMap<String, String> additionalFields = new LinkedMultiValueMap<>();
        additionalFields.put("tags.value_smv", WP_REASONS_PDF_TAGS);

        updateResponse = searchService.updateFile(DEFAULT_SITE, WP_REASONS_PDF_DOC_ID, file, additionalFields);
        logger.info(updateResponse);

        commitResponse = searchService.commit();
        logger.info(commitResponse);

        results = searchService.search(query);
        assertNotNull(results);

        response = getQueryResponse(results);
        assertEquals(2, getNumDocs(response));

        docs = getDocs(response);
        ipadDoc = docs.get(IPAD_DOC_ID);
        wpReasonsPdfDoc = docs.get(WP_REASONS_PDF_DOC_ID);

        assertNotNull(ipadDoc);
        assertNotNull(wpReasonsPdfDoc);
        assertIPadDoc(ipadDoc, DEFAULT_SITE);
        assertWpReasonsPdfDocWithAdditionalFields(wpReasonsPdfDoc, DEFAULT_SITE);

        String deleteResponse = searchService.delete(DEFAULT_SITE, IPAD_DOC_ID);
        logger.info(deleteResponse);

        deleteResponse = searchService.delete(DEFAULT_SITE, WP_REASONS_PDF_DOC_ID);
        logger.info(deleteResponse);

        commitResponse = searchService.commit();
        logger.info(commitResponse);

        results = searchService.search(query);
        assertNotNull(results);

        response = getQueryResponse(results);
        assertEquals(0, getNumDocs(response));
    }

    @Test
    public void testMethodsWithIndexId() throws Exception {
        SolrQuery query = searchService.createQuery();
        query.setQuery("*:*");

        Map<String, Object> results = searchService.search(PLUTON_INDEX_ID, query);
        assertNotNull(results);

        Map<String, Object> response = getQueryResponse(results);
        assertEquals(0, getNumDocs(response));

        String xml = getClasspathFileContent("docs/" + IPAD_DOC_ID);
        String updateResponse = searchService.update(PLUTON_INDEX_ID, PLUTON_SITE, IPAD_DOC_ID, xml, true);
        logger.info(updateResponse);

        File file = getClasspathFile("docs/" + WP_REASONS_PDF_DOC_ID);
        updateResponse = searchService.updateFile(PLUTON_INDEX_ID, PLUTON_SITE, WP_REASONS_PDF_DOC_ID, file);
        logger.info(updateResponse);

        String commitResponse = searchService.commit(PLUTON_INDEX_ID);
        logger.info(commitResponse);

        results = searchService.search(PLUTON_INDEX_ID, query);
        assertNotNull(results);

        response = getQueryResponse(results);
        assertEquals(2, getNumDocs(response));

        Map<String, Map<String, Object>> docs = getDocs(response);
        Map<String, Object> ipadDoc = docs.get(IPAD_DOC_ID);
        Map<String, Object> wpReasonsPdfDoc = docs.get(WP_REASONS_PDF_DOC_ID);

        assertNotNull(ipadDoc);
        assertNotNull(wpReasonsPdfDoc);
        assertIPadDoc(ipadDoc, PLUTON_SITE);
        assertWpReasonsPdfDoc(wpReasonsPdfDoc, PLUTON_SITE);

        MultiValueMap<String, String> additionalFields = new LinkedMultiValueMap<>();
        additionalFields.put("tags.value_smv", WP_REASONS_PDF_TAGS);

        updateResponse = searchService.updateFile(PLUTON_INDEX_ID, PLUTON_SITE, WP_REASONS_PDF_DOC_ID, file,
                                                  additionalFields);
        logger.info(updateResponse);

        commitResponse = searchService.commit(PLUTON_INDEX_ID);
        logger.info(commitResponse);

        results = searchService.search(PLUTON_INDEX_ID, query);
        assertNotNull(results);

        response = getQueryResponse(results);
        assertEquals(2, getNumDocs(response));

        docs = getDocs(response);
        ipadDoc = docs.get(IPAD_DOC_ID);
        wpReasonsPdfDoc = docs.get(WP_REASONS_PDF_DOC_ID);

        assertNotNull(ipadDoc);
        assertNotNull(wpReasonsPdfDoc);
        assertIPadDoc(ipadDoc, PLUTON_SITE);
        assertWpReasonsPdfDocWithAdditionalFields(wpReasonsPdfDoc, PLUTON_SITE);

        String deleteResponse = searchService.delete(PLUTON_INDEX_ID, PLUTON_SITE, IPAD_DOC_ID);
        logger.info(deleteResponse);

        deleteResponse = searchService.delete(PLUTON_INDEX_ID, PLUTON_SITE, WP_REASONS_PDF_DOC_ID);
        logger.info(deleteResponse);

        commitResponse = searchService.commit(PLUTON_INDEX_ID);
        logger.info(commitResponse);

        results = searchService.search(query);
        assertNotNull(results);

        response = getQueryResponse(results);
        assertEquals(0, getNumDocs(response));
    }

    private File getClasspathFile(String path) throws IOException {
        return new ClassPathResource(path).getFile();
    }

    private String getClasspathFileContent(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream());
    }

    private Map<String, Object> getQueryResponse(Map<String, Object> results) {
        return (Map<String, Object>)results.get("response");
    }

    private int getNumDocs(Map<String, Object> response) {
        return (Integer)response.get("numFound");
    }

    private Map<String, Map<String, Object>> getDocs(Map<String, Object> response) {
        List<Map<String, Object>> docList = (List<Map<String, Object>>)response.get("documents");
        Map<String, Map<String, Object>> docs = new HashMap<>(3);

        for (Map<String, Object> doc : docList) {
            docs.put((String)doc.get("localId"), doc);
        }

        return docs;
    }

    private void assertIPadDoc(Map<String, Object> doc, String site) {
        long date = ISODateTimeFormat.dateTime().parseDateTime("2014-10-01T00:00:00.000Z").getMillis();

        assertEquals(site, doc.get("crafterSite"));
        assertEquals(site + ":" + IPAD_DOC_ID, doc.get("id"));
        assertEquals(IPAD_DOC_ID, doc.get("localId"));
        assertEquals("iPad Air 64GB", doc.get("name"));
        assertEquals("Apple MH182LL/A iPad Air 9.7-Inch Retina Display 64GB, Wi-Fi (Gold)",
                     doc.get("description_html").toString().trim());
        assertEquals(date, doc.get("availableDate_dt"));
        assertEquals(Arrays.asList("Apple", "iPad", "Tablet"), doc.get("tags.value_smv"));
    }

    private void assertWpReasonsPdfDoc(Map<String, Object> doc, String site) {
        assertEquals(site, doc.get("crafterSite"));
        assertEquals(site + ":" + WP_REASONS_PDF_DOC_ID, doc.get("id"));
        assertEquals(WP_REASONS_PDF_DOC_ID, doc.get("localId"));
        assertNotNull(doc.get("content"));
    }

    private void assertWpReasonsPdfDocWithAdditionalFields(Map<String, Object> doc, String site) {
        assertWpReasonsPdfDoc(doc, site);

        assertEquals(WP_REASONS_PDF_TAGS, doc.get("tags.value_smv"));
    }

}
