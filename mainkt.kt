import java.lang.IllegalArgumentException
import kotlin.system.exitProcess

fun int_assert(prompt: String): Int{
    loop@while (true){
        print(prompt + "\n>\t")
        val usrinput = readLine().toString()
        return try {val number_input = usrinput.toInt(); number_input}
        catch(exception: IllegalArgumentException){
            println("Not a valid option.")
            continue@loop
        }
    }
}

class Player(var name: String, var currenthealth: Int, val maxhealth: Int, var attack: Int, var weapon_equipped: Weapon, var defense: Int, var bag: MutableMap<Int, Consumable>, var credits: Int){
    fun equip_weapon(weapon: Weapon){
        if (weapon_equipped.name != "Fisticuffs"){
            unequip_weapon(weapon = weapon_equipped)
        }
        attack += weapon.attack
        weapon.equipped = true
        weapon_equipped = weapon
        println("$name has equipped ${weapon.name}. They now have $attack attack.")
    }
    fun unequip_weapon(weapon: Weapon){
        attack -= weapon.attack
        weapon.equipped = false
        println("$name has unequipped ${weapon.name}. Their attack is now $attack.")
        weapon_equipped = fisticuffs
        fisticuffs.equipped = true
    }
    fun attack(target: Enemy){
        val damage = attack - target.defense
        val resultant_health = target.currenthealth - damage
        if (resultant_health < 0){target.currenthealth = 0}else{target.currenthealth -= damage}
        println("$name attacked ${target.name} for $damage!\n${target.name} has ${target.currenthealth} HP remaining.")
    }
    fun open_bag(bag: MutableMap<Int, Consumable>){
        var bag_contents = ""
        for ((index, item) in bag.entries){
            bag_contents += "${index + 1}: ${item.name}"
        }
        println(bag_contents)
    }
}

class Enemy(val name: String, var currenthealth: Int, val maxhealth: Int, var attack: Int, var defense: Int){
    fun heal(heal_amount: Int){
        currenthealth += heal_amount
    }
    fun attack(target: Player){
        val damage = attack - target.defense
        val resultant_health = target.currenthealth - damage
        if (resultant_health < 0){target.currenthealth = 0}else{target.currenthealth -= damage}
        println("$name attacked ${target.name} for $damage!\n${target.name} has ${target.currenthealth} HP remaining.")
    }
}

class Consumable(var name: String, var heals: Int, var shop_cost: Int){
    fun use(target: Player){
        val resultant_health: Int = heals + target.currenthealth
        if (resultant_health > target.currenthealth) {target.currenthealth = target.maxhealth} else target.currenthealth = resultant_health
    }
}

class Weapon(var name: String, var attack: Int, var equipped: Boolean, var shop_cost: Int)

var fisticuffs = Weapon(name = "Fisticuffs", attack = 0, equipped = true, shop_cost = 0)
var wooden_sword = Weapon(name = "Wooden Sword", attack = 5, equipped = false, shop_cost = 10)
var iron_sword = Weapon(name = "Iron Sword", attack = 12, equipped = false, shop_cost = 50)
var suzaku = Weapon(name = "Suzaku", attack = 17, equipped = false, shop_cost = 250)
val basic_hp = Consumable(name = "Basic Health Potion", heals = 5, shop_cost = 10)
val big_hp = Consumable(name = "Big Health Potion", heals = 15, shop_cost = 20)
var jarnold = Player(name = "", currenthealth = 25, maxhealth = 25, attack = 10, weapon_equipped =  fisticuffs, defense = 5, bag = mutableMapOf(1 to basic_hp), credits = 99999)
var goblin = Enemy(name = "Goblin", currenthealth = 25, maxhealth = 25, attack = 16, defense = 7)
var magus = Enemy(name = "Magus", currenthealth = 60, maxhealth = 60, attack = 25, defense = 12)
var enemies_defeated = mutableListOf<Enemy>()

fun arena(){
    battleloop@while (true){
        var opponent: Enemy
        print("Welcome to the arena! Would you like to battle?\n[Y/N]\n>\t")
        val battle_choice = readLine()?.capitalize() ?: continue
        if (battle_choice !in mutableListOf("Y", "N")){println("Invalid choice."); continue}
        when (battle_choice){
            "Y" -> {opponent = if (goblin !in enemies_defeated){goblin}else{magus}; battle(player = jarnold, enemy = opponent)}
            "N" -> break@battleloop
        }
    }
}

fun battle(player: Player, enemy: Enemy){
    var conceded = false
    battlerun@while (player.currenthealth > 0 && enemy.currenthealth > 0){
        val player_action = int_assert(prompt = "What will ${player.name} do?\n1: Attack\n2: Use Item\n3: Concede")
        if (player_action !in mutableListOf(1, 2, 3)){println("Invalid choice."); continue}
        when (player_action){
            1 -> {player.attack(target = enemy); if (enemy.currenthealth > 0){enemy.attack(target = player)}else{break@battlerun}}
            2 -> {player.open_bag(bag = player.bag); if (enemy.currenthealth > 0){enemy.attack(target = player)}else{break@battlerun}}
            3 -> {conceded = true; break@battlerun}
        }
    }
    if (player.currenthealth <= 0){
        println("${player.name} fell in battle!")
        player.currenthealth = player.maxhealth
        println("The arena's healers were able to nurse ${player.name} back to health.")
        navigation()
    }
    else if (enemy.currenthealth <= 0){
        println("${enemy.name} died!\n${player.name} got 100 credits!")
        player.credits += 100
        if (enemy !in enemies_defeated){enemies_defeated.add(element = enemy)}
        if (enemies_defeated.size >= 2){println("You win! Thanks for playing!"); exitProcess(status = -1)}
        enemy.currenthealth = enemy.maxhealth
        navigation()
    }
    else if (conceded == true){
        println("${player.name} ran! They dropped 20 credits.")
        if (player.credits < 20){player.credits = 0}else{player.credits -= 20}
        player.currenthealth = player.maxhealth
        enemy.currenthealth = enemy.maxhealth
        println("The arena's healers nursed ${player.name} and ${enemy.name} back to health.")
        navigation()
    }
}

fun home(){
    homeloop@while (true){
        print("${jarnold.name} returned home. Rest and recover HP?\n[Y/N]\n>\t")
        val will_rest = readLine()?.capitalize() ?: continue
        if (will_rest !in mutableListOf("Y", "N")){println("That isn't an option."); continue}
        when (will_rest){
            "Y" -> {jarnold.currenthealth = jarnold.maxhealth; println("${jarnold.name} rested. Their HP is now ${jarnold.currenthealth} / ${jarnold.maxhealth}"); break@homeloop}
            "N" -> break@homeloop
        }
    }
    navigation()
}

fun shop(){
    val shop_consumables: MutableMap<Int, Consumable> = mutableMapOf(1 to basic_hp, 2 to big_hp)
    val shop_weapons: MutableMap<Int, Weapon> = mutableMapOf(1 to wooden_sword, 2 to iron_sword, 3 to suzaku)
    shoploop@while (true){
        val customer_selection = int_assert(prompt = "Welcome to the shop. What are you interested in?\n1: Consumables\n2: Weapons\n3: Exit shop")
        var displayString = ""
        when (customer_selection){
            !in mutableListOf(1,2,3) -> {println("Invalid choice."); continue@shoploop}
            1 -> {
                for (entry in shop_consumables.entries){displayString += "${entry.key}: ${entry.value.name}\n"}
                val customer_purchase = int_assert(prompt = "Buy which item?\n${displayString.trim()}")
                if (customer_purchase !in shop_consumables.keys){println("Not an option."); continue@shoploop}
                val item_to_purchase = shop_consumables[customer_purchase] ?: continue@shoploop
                val purchase_quantity = int_assert(prompt = "How many ${item_to_purchase.name}s would you like?")
                if ((purchase_quantity * item_to_purchase.shop_cost) > jarnold.credits){println("You can't afford that."); continue@shoploop}
                else{
                    for (number in 0..purchase_quantity){jarnold.bag[number] = item_to_purchase}
                    jarnold.credits -= (purchase_quantity * item_to_purchase.shop_cost)
                    println("Added ${purchase_quantity} ${item_to_purchase.name}(s) to the bag.")
                }
            }
            2 -> {
                for (entry in shop_weapons.entries){displayString += "${entry.key}: ${entry.value.name}\n"}
                val customer_purchase = int_assert(prompt = "Buy which item?\n${displayString.trim()}")
                if (customer_purchase !in shop_weapons.keys){println("Invalid choice."); continue@shoploop}
                val weapon_to_purchase = shop_weapons[customer_purchase] ?: continue@shoploop
                if (weapon_to_purchase.shop_cost > jarnold.credits){println("You can't afford that."); continue@shoploop}
                else{println("Purchased ${weapon_to_purchase.name}"); jarnold.equip_weapon(weapon = weapon_to_purchase)}
            }
            3 -> navigation()
        }
    }
}

fun exit_game(){exitProcess(status = -1)}

fun navigation() {
    val destinations = mutableMapOf(1 to ::arena, 2 to ::home, 3 to ::shop, 4 to ::exit_game)
    var player_choice: Int
    while (true) {
        try {
            print("Go where?\n1: Arena\n2: Home\n3: Shop\n4: Exit Game\n>\t")
            player_choice = readLine()?.toInt() ?: continue
            if (player_choice !in mutableListOf(1, 2, 3, 4)){throw ArrayIndexOutOfBoundsException()}else{break}
        }
        catch (exception: ArrayIndexOutOfBoundsException) {println("That isn't an option."); continue}
        catch (exception: IllegalArgumentException) {println("That isn't an option."); continue}
    }
    destinations[player_choice]!!()
}

fun main(){
    print("Input your name: ")
    jarnold.name = readLine()?.capitalize() ?: "Jarnold"
    navigation()
}