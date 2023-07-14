package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.service.markdown.WikiConfluenceSerializer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;

import java.util.Map;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class MarkdownService {

    private final PegDownProcessor pegDownProcessor;

    public MarkdownService() {
        pegDownProcessor = new PegDownProcessor(WikiConfluenceSerializer.extensions());
    }

    public MarkdownService(long parseTimeOut) {
        this.pegDownProcessor = new PegDownProcessor(WikiConfluenceSerializer.extensions(), parseTimeOut);
    }

    public String convertMarkdown2Wiki(final String markdown, Map<String, String> pageVariables) {
        final RootNode root = pegDownProcessor.parseMarkdown(markdown.toCharArray());
        WikiConfluenceSerializer ser =  new WikiConfluenceSerializer(pageVariables) {
            @Override
            protected void notImplementedYet(Node node) {
                final int[] lc = WikiConfluenceSerializer.lineAndColFromNode( markdown, node);
                throw new UnsupportedOperationException( String.format("Node [%s] not supported yet. line=[%d] col=[%d]",
                        node.getClass().getSimpleName(),
                        lc[0],
                        lc[1] ));
            }
        };

        root.accept(ser);

        return ser.toString();
    }
}
