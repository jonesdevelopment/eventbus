## :comet: EventBus

> **Note** Made in under 25 minutes

### Usage
```java
public class YourClass implements jones.eventbus.api.Listener /* This part is important */ {
  private final jones.eventbus.EventBus eventBus = new jones.eventbus.EventBus(); // we need an instance

  // register event handler
  public void registerEventHandler() {
    eventBus.register(this);
  }

  // unregister event handler
  public void unregisterEventHandler() {
    eventBus.unregister(this);
  }

  // subscribe an event
  @jones.eventbus.api.Subscribe
  // event can also be a custom event (your class must extend jones.eventbus.api.Event)
  public void handle(jones.eventbus.api.Event event) {
    // do stuff
  }
}
```

#### Custom Events
```java
public class YourEvent extends jones.eventbus.api.Event {
  public double randomNumber; // example

  public YourEvent(double randomNumber) {
    this.randomNumber = randomNumber;
  }
}
```
