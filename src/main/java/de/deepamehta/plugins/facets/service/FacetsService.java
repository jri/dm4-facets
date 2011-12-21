package de.deepamehta.plugins.facets.service;

import de.deepamehta.core.Topic;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.PluginService;



public interface FacetsService extends PluginService {

    void associateWithFacetType(long topicId, String facetTypeUri);

    /**
     * Stores a topic facet in the DB.
     *
     * @param   topic           The topic to be facetted.
     * @param   facetTypeUri    URI of the facet type.
     * @param   facet           The facet to store.
     *
     * @return  The stored facet.
     */
    Topic setFacet(Topic topic, String facetTypeUri, TopicModel facet, ClientState clientState, Directives directives);

    /**
     * Retrieves a topic facet from the DB.
     *
     * @param   topic           The facetted topic.
     * @param   facetTypeUri    URI of the facet type.
     *
     * @return  The retrieved facet.
     */
    Topic getFacet(Topic topic, String facetTypeUri);
}
