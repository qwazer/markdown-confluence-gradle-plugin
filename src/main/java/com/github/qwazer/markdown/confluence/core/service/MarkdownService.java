package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.service.markdown.WikiConfluenceSerializer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;
import org.springframework.stereotype.Service;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class MarkdownService {

    private PegDownProcessor pegDownProcessor = new PegDownProcessor(WikiConfluenceSerializer.extensions());

    MarkdownService() {

    }

    MarkdownService(long parseTimeOut) {
        this.pegDownProcessor = new PegDownProcessor(WikiConfluenceSerializer.extensions(), parseTimeOut);
    }

    public String convertMarkdown2Wiki(final String s, ConfluenceConfig confluenceConfig) {

        final RootNode root = pegDownProcessor.parseMarkdown(s.toCharArray());

        WikiConfluenceSerializer ser =  new WikiConfluenceSerializer(confluenceConfig.getPageVariables()) {

            @Override
            protected void notImplementedYet(Node node) {


                final int lc[] = WikiConfluenceSerializer.lineAndColFromNode( s, node);
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
