package com.opendatasoft.elasticsearch.index.analysis;

import com.opendatasoft.lucene.analysis.synonym.SynonymReloaderFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.index.settings.IndexSettings;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

import java.io.Reader;
import java.util.Map;

import static com.opendatasoft.elasticsearch.index.config.SynonymsElasticConfigurator.init;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

@AnalysisSettingsRequired
public class SynonymReloaderTokenFilterFactory extends AbstractTokenFilterFactory {

    private final boolean ignoreCase;
    private final SynonymMapReloader synonymMapReloader;

    @Inject
    public SynonymReloaderTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env, IndicesAnalysisService indicesAnalysisService, Map<String, TokenizerFactoryFactory> tokenizerFactories,
                                             @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        init(indexSettings, settings);
        this.ignoreCase = settings.getAsBoolean("ignore_case", false);
        String tokenizerName = settings.get("tokenizer", "whitespace");
        TokenizerFactoryFactory tokenizerFactoryFactory = tokenizerFactories.get(tokenizerName);
        if (tokenizerFactoryFactory == null) {
            tokenizerFactoryFactory = indicesAnalysisService.tokenizerFactoryFactory(tokenizerName);
        }
        if (tokenizerFactoryFactory == null) {
            throw new ElasticsearchIllegalArgumentException("failed to find tokenizer [" + tokenizerName + "] for synonym token filter");
        }
        ImmutableSettings.Builder settingsBuilder = settingsBuilder().put(indexSettings).put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT);
        final TokenizerFactory tokenizerFactory = tokenizerFactoryFactory.create(tokenizerName, settingsBuilder.build());

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
                Tokenizer tokenizer = tokenizerFactory == null ? new WhitespaceTokenizer(reader) : tokenizerFactory.create(reader);
                TokenStream stream = ignoreCase ? new LowerCaseFilter(tokenizer) : tokenizer;
                return new TokenStreamComponents(tokenizer, stream);
            }
        };
        this.synonymMapReloader = new SynonymMapReloader(env, settings, analyzer);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        // fst is null means no synonyms
        return synonymMapReloader.getSynonymMap().fst == null ? tokenStream : new SynonymReloaderFilter(tokenStream, synonymMapReloader, ignoreCase);
    }
}