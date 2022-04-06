#!/bin/sh
if ! [ -d classes ];
then
   mkdir classes
fi
javac -cp . -d classes src/main/java/ir/Engine.java src/main/java/ir/HashedIndex.java src/main/java/ir/HITSRanker.java src/main/java/ir/Index.java src/main/java/ir/Indexer.java src/main/java/ir/KGramIndex.java src/main/java/ir/KGramPostingsEntry.java src/main/java/ir/PersistentHashedIndex.java src/main/java/ir/PostingsEntry.java src/main/java/ir/PostingsList.java src/main/java/ir/Query.java src/main/java/ir/QueryType.java src/main/java/ir/RankingType.java src/main/java/ir/Searcher.java src/main/java/ir/SearchGUI.java src/main/java/ir/SpellChecker.java src/main/java/ir/SpellingOptionsDialog.java src/main/java/ir/Tokenizer.java src/main/java/ir/TokenTest.java 
