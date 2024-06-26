package it.unibo.pcd.akka.cluster.advanced

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.*
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import it.unibo.pcd.akka.cluster.seeds
import concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps
import scala.util.Random
import it.unibo.pcd.akka.cluster.*
import akka.actor.typed.ActorRef

// Server.
object Root:

  // Crea una lobby.
  def lobby(): Behavior[Nothing] =
    Behaviors.setup { ctx =>
      val cluster = Cluster(ctx.system)
      require(cluster.selfMember.hasRole(Roles.backend))
      ctx.spawnAnonymous(LobbyActor.apply())
      Behaviors.empty
    }

  // Crea un player.
  def player(playerID: String, onActorCreated: (PlayerActor) => Unit): Behavior[Nothing] =
    Behaviors.setup { ctx =>
      val cluster = Cluster(ctx.system)
      require(cluster.selfMember.hasRole(Roles.frontend))  
      ctx.spawnAnonymous(PlayerActor.apply(playerID, onActorCreated))
      Behaviors.empty
    }

@main def startLobby: Unit =
  startupWithRole(Roles.backend, 2551)(Root.lobby())