package nl.hva.ict.sm3.backend.utils.xml;

public record VotesTransformers(
        VotesTransformer resultTransformer,
        VotesTransformer nationalVotesTransformer,
        VotesTransformer constituencyVotesTransformer,
        VotesTransformer municipalityVotesTransformer
) {}
