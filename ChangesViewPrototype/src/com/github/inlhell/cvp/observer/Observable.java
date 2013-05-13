package com.github.inlhell.cvp.observer;

public interface Observable {
    public void registerObserver(Observer observer);
    
    public void removeObserver(Observer observer);
    
    public void notifyObservers();
}