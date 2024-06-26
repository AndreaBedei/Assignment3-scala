package it.unibo.pcd.akka.cluster.advanced

import javax.swing.*
import java.awt.*
import scala.collection.immutable.List
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import it.unibo.pcd.akka.cluster.startupWithRole
import it.unibo.pcd.akka.cluster.advanced.messages.PlayerMessage
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import it.unibo.pcd.akka.cluster.advanced.messages.Die

object SimpleGameGUI extends App {
  // Da questa callback gli viene restituito l'attore che è stato creato.
  private val onPlayerActorCreated = (actor: PlayerActor) => 
    val frame = new JFrame("Game GUI")
    frame.setSize(400, 200)
    frame.setLayout(new BorderLayout())

    var starting = true
    var noGameSelected = true

    val topPanel = new JPanel()
    
    val createGameButton = new JButton("Create Game")
    createGameButton.setEnabled(false)
    topPanel.add(createGameButton)
    frame.add(topPanel, BorderLayout.NORTH)

    val centerPanel = new JPanel()
    centerPanel.setLayout(new FlowLayout())

    val joinButton = new JButton("Join")
    joinButton.setEnabled(false)
    
    val dropdownMenu = new JComboBox[String]()
    centerPanel.add(dropdownMenu)
    centerPanel.add(joinButton)
    frame.add(centerPanel, BorderLayout.CENTER)
    
    // Esso serve quando l'attore riceve il messagio per le partite in corso per aggiornare la lista.
    actor.setOnGamesUpdated:
      (games: List[(String, ActorRef[PlayerMessage])]) =>
        if starting then
          createGameButton.setEnabled(true)
          starting = false
        dropdownMenu.removeAllItems()
        if games.length == 0 then joinButton.setEnabled(false)
        else
          for 
            g <- games
            name = g._1
          do dropdownMenu.addItem(name)
          joinButton.setEnabled(true)
        dropdownMenu.invalidate()
        frame.repaint()

    createGameButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        noGameSelected = false
        frame.setVisible(false)
        actor.startNewGame() // Si inizia un nuovo gioco.
      }
    })

    joinButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        noGameSelected = false
        frame.setVisible(false)
        val selectedChoice = dropdownMenu.getSelectedItem()
        actor.joinGame(selectedChoice.toString()) // Si partecipa a una partita già creata.
      }
    })

    frame.setVisible(true)

    frame.addWindowListener(new WindowAdapter {
      override def windowClosing(x: WindowEvent): Unit =
        // Viene mandato un messaggio all'attore di morte se non ha ancora partecipato a una partita quando ha chiuso la finestra.
        if (noGameSelected)
        then
          actor.context.self ! Die
          System.exit(0)
    })

  // Si crea l'attore giocatore e gli si passa il callback per inizializzare la gui alla sua creazione.
  // onPlayerActorCreated callback.
  startupWithRole(Roles.frontend, 0)(Root.player("P" + System.currentTimeMillis().hashCode().toString, onPlayerActorCreated))
}