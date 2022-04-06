"""
References: 
https://towardsdatascience.com/normalized-discounted-cumulative-gain-37e6f75090e9
"""

import numpy as np
import pandas as pd

def discountedCumulativeGain(result):
    dcg = []
    for idx, val in enumerate(result): 
        numerator = 2**val - 1
        # add 2 because python 0-index
        denominator =  np.log2(idx + 2) 
        score = numerator/denominator
        dcg.append(score)
    return sum(dcg)

def normalizedDiscountedCumulativeGain(result, sorted_result): 
    dcg = discountedCumulativeGain(result)
    idcg = discountedCumulativeGain(sorted_result)
    ndcg = dcg / idcg
    return ndcg

# document = 'average_relevance_filtered.txt'
document = 'feedbacked_result_relevance.txt'
relevances = pd.read_csv(document, sep=' ', names=['file', 'rank'])    
ndcg = normalizedDiscountedCumulativeGain(np.array(relevances['rank']), np.array(relevances['rank'].sort_values(ascending=False)))
print("The gain of {} is {}".format(document, ndcg))


