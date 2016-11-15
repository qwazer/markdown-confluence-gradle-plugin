package com.github.qwazer.markdown.confluence.core.service.impl;

import com.github.qwazer.markdown.confluence.core.service.markdown.WikiConfluenceSerializer;
import com.github.qwazer.markdown.confluence.core.service.Markdown2WikiService;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;
import org.springframework.stereotype.Service;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class Markdown2WikiServiceImpl implements Markdown2WikiService {

    final PegDownProcessor pegDownProcessor = new PegDownProcessor(WikiConfluenceSerializer.extensions());

    @Override
    public String convertMarkdown2Wiki(final String s) {

        final RootNode root = pegDownProcessor.parseMarkdown(s.toCharArray());

        WikiConfluenceSerializer ser =  new WikiConfluenceSerializer() {

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
