package com.paper.reviewer.ai.parser;

public record FieldAnalysis(String title, String language, String primaryDiscipline,
                            String secondaryDiscipline, String researchParadigm,
                            String methodologyType, String targetJournalTier, String paperMaturity) { }
