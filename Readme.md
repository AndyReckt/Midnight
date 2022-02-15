# Midnight
> A Java Redis PubSub helper which let you send full objects without need of manual serializing.

Credit goes to [Joeleoli's Pidgin](https://github.com/joeleoli/pidgin) for the original idea of this, and to [ThatKawaiiSam](https://github.com/thatkawaiisam/) for the readme.

---

## How to setup


#### Create the midnight instance
```java
public class ExampleClass {
    public static void main(final String[] args) {
        // Create ur JedisPool
        JedisPool pool = new JedisPool("localhost", 6379);
        
        // Start the instance
        Midnight midnight = new Midnight(pool);
    }
}

```
#### Registering a class

```
midnight.registerObject(ExampleObject.class);
midnight.registerListener(new ExampleSubscriber());
```

#### Sending an object

```
midnight.sendObject(new ExampleObject("exampleParameter"));
```

---

## Examples

[Main Class](https://github.com/AndyReckt/Midnight/blob/main/src/main/java/me/andyreckt/midnight/example/Run.java)
| [Example Object](https://github.com/AndyReckt/Midnight/blob/main/src/main/java/me/andyreckt/midnight/example/ExampleObject.java)
| [Example Listener](https://github.com/AndyReckt/Midnight/blob/main/src/main/java/me/andyreckt/midnight/example/ExampleSubscriber.java)

---

## Contributing
When contributing, please create a pull request with the branch named as follows ``<feature/fix>/<title>``.

To compile, run the maven command: ``mvn clean install``

---

## Contact

- Discord: AndyReckt#0001
- Telegram: [AndyReckt](https://t.me/andyreckt)
