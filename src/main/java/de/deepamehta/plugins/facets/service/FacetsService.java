package de.deepamehta.plugins.facets.service;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.PluginService;



public interface FacetsService extends PluginService {

    void associateWithFacetType(long topicId, String facetTypeUri);

    /**
     * @return  The facet added.
     */
    Topic addFacet(Topic topic, String facetTypeUri, TopicModel facet, ClientState clientState, Directives directives);

    Topic getFacet(Topic topic, String facetTypeUri);
}
