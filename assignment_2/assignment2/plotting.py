import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import pandas as pd

"""
The amount of sinks influences the performance of algorithm 5 significantly! Most of the times the surfer gets in a sink very quick.
This way most documents count up equally not presenting the inherent information of the "graph".
"""
def plot27():
    x_axis = [5000,10000, 24221,48442,100000,242210,1000000]
    y_data = np.array([ # [x=[],y=[]]
                            [2.395102E-4, 2.3034219999999998E-4, 2.2484807044258122E-4, 2.254318841409201E-4, 2.257234E-4, 2.2813250267167718E-4, 2.2556859599999997E-4], #MC1
                            [2.2750713265915905E-4, 2.273152259223211E-4, 2.2244842127527528E-4, 2.371476407121454E-4, 2.204396163357621E-4, 2.244152220340173E-4, 2.2931041036098685E-4], #MC2
                            [2.681422E-4, 0.01233382121234568, 0.010016142200000003, 2.681422E-4, 2.681422E-4, 0.009240795261224488, 2.681422E-4], #MC4
                            [2.681422E-4, 2.681422E-4, 2.681422E-4, 2.681422E-4, 2.681422E-4, 2.681422E-4, 2.681422E-4]  #MC5
                            ])

    y_data_m4 = np.array([ # [x=[],y=[]]
                                [2.047902E-4, 2.187422E-4, 2.2929527585750968E-4, 2.253266344008936E-4, 2.286214E-4, 2.2654160443820714E-4, 2.2529423200000006E-4],
                                [2.2639430313025225E-4, 2.2803557548694843E-4, 2.2569299517090298E-4, 2.2382147038968938E-4, 2.2630117967513858E-4, 2.2737055864450437E-4, 2.2864324979172994E-4],
                                [2.681422E-4, 2.681422E-4, 0.005991455809467456, 0.005991455809467456, 2.681422E-4, 2.681422E-4, 2.681422E-4],
                                [2.681422E-4, 0.013887796179238755, 2.681422E-4, 2.681422E-4, 2.681422E-4, 2.681422E-4, 2.681422E-4]
                                ])

    fig, (ax1,ax2,ax3,ax4) = plt.subplots(4)
    fig.suptitle("Monte carlo method comparison")
    ax1.plot(x_axis, y_data[0], color='r', label="MC1")
    ax2.plot(x_axis, y_data[1], color='g',  label="MC2")
    ax3.plot(x_axis, y_data[2], color='b',  label="MC4")
    ax4.plot(x_axis, y_data[3], color='y',  label="MC5")
    ax1.legend()
    ax2.legend()
    ax3.legend()
    ax4.legend()
    """
    ax2.plot(x_axis, y_data_m4[0], label="MC1")
    ax2.plot(x_axis, y_data_m4[1], label="MC2")
    ax2.plot(x_axis, y_data_m4[2], label="MC4")
    ax2.plot(x_axis, y_data_m4[3], label="MC5")
    ax2.legend()
    """
    #plt.show()
    plt.savefig('plots/plots_27_mc_methods.png')

def plot24():
    steps = range(10,51, 10)
    precission = np.array([(5/10),(5/20),(7/30),(8/80),(9/50)])
    recall  = np.array([(5/100),(5/100),(7/100),(8/100),(9/100)])
    
    precission_unranked = np.array([(5/10),(7/20),(8/30),(8/40),(8/50)])
    recall_unranked = np.array([(5/100),(7/100),(8/100),(8/100),(8/100)])

    fig, (ax1,ax2) = plt.subplots(2)
    fig.suptitle("Precission/ recall compareison")
    ax1.title.set_text("ranked retreval")
    ax1.plot(steps, precission, label='Precission')
    ax1.plot(steps, recall, label='Recall')
    ax1.legend()
    ax2.title.set_text("unranked retreval")
    ax2.plot(steps, precission_unranked, label='Precission')
    ax2.plot(steps, recall_unranked, label='Recall')
    ax2.legend()
    
    plt.savefig('plots/plots_24_prec_rec.png')

plot24()
plot27()